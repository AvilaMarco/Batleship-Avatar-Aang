import { cleanup, fireEvent, render, screen, waitFor, within, act } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import GameScreen from "./GameScreen";
import { ApiError } from "../api/http";
import { view } from "../domain/fixtures";
import type { GameHandlers } from "../api/stomp";

const m = vi.hoisted(() => ({ getGame: vi.fn(), placeShips: vi.fn(), fireSalvo: vi.fn(), sendEmote: vi.fn(), connectGame: vi.fn(), disconnect: vi.fn() }));
vi.mock("../api/games", () => ({ getGame: m.getGame, placeShips: m.placeShips, fireSalvo: m.fireSalvo }));
vi.mock("../api/stomp", () => ({ connectGame: m.connectGame }));

let handlers: GameHandlers;
const toPlace = () => view({ status: "CREATED", me: { ships_placed: false }, opponent: {} });

beforeEach(() => {
  m.getGame.mockResolvedValue(toPlace());
  m.connectGame.mockImplementation((_id: number, h: GameHandlers) => {
    handlers = h;
    return { sendEmote: m.sendEmote, disconnect: m.disconnect };
  });
});
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

async function placeFleet() {
  const names = ["Portaaviones", "Acorazado", "Submarino", "Destructor", "Patrullero"];
  for (const [i, n] of names.entries()) {
    fireEvent.click(await screen.findByRole("button", { name: new RegExp(n, "i") }));
    fireEvent.click(screen.getByRole("button", { name: `${"ABCDE"[i]}1` }));
  }
}

it("coloca 5 barcos por clic y envía la flota", async () => {
  m.placeShips.mockResolvedValue(view({ me: { ships_placed: true }, opponent: { ships_placed: false } }));
  render(<GameScreen id={1} />);
  const send = await screen.findByRole("button", { name: /enviar barcos/i });
  expect(send).toBeDisabled();
  await placeFleet();
  expect(send).toBeEnabled();
  fireEvent.click(send);
  await waitFor(() => expect(m.placeShips).toHaveBeenCalledTimes(1));
  const [id, ships] = m.placeShips.mock.calls[0];
  expect(id).toBe(1);
  expect(ships).toHaveLength(5);
  expect(ships[0]).toEqual({ type: "CARRIER", cells: ["A1", "A2", "A3", "A4", "A5"] });
  expect(await screen.findByText(/esperando.*barcos/i)).toBeInTheDocument();
});

it("clic en un barco colocado lo quita", async () => {
  render(<GameScreen id={1} />);
  fireEvent.click(await screen.findByRole("button", { name: /patrullero/i }));
  fireEvent.click(screen.getByRole("button", { name: "A1" }));
  expect(screen.getByRole("button", { name: "A2" })).toHaveClass("ship");
  fireEvent.click(screen.getByRole("button", { name: "A2" }));
  expect(screen.getByRole("button", { name: "A1" })).not.toHaveClass("ship");
});

it("GAME_STARTED vuelve a pedir la partida; EMOTE no", async () => {
  render(<GameScreen id={1} />);
  await screen.findByRole("button", { name: /enviar barcos/i });
  expect(m.getGame).toHaveBeenCalledTimes(1);
  handlers.onEvent({ type: "EMOTE", game_id: 1, player_id: 2, emote: "x" });
  expect(m.getGame).toHaveBeenCalledTimes(1);
  handlers.onEvent({ type: "GAME_STARTED", game_id: 1, turn: 1 });
  await waitFor(() => expect(m.getGame).toHaveBeenCalledTimes(2));
});

it("INVALID_FLEET muestra el detalle", async () => {
  m.placeShips.mockRejectedValue(new ApiError({ status: 422, code: "INVALID_FLEET", detail: "Flota inválida" }));
  render(<GameScreen id={1} />);
  await screen.findByRole("button", { name: /enviar barcos/i });
  await placeFleet();
  fireEvent.click(screen.getByRole("button", { name: /enviar barcos/i }));
  expect(await screen.findByRole("alert")).toHaveTextContent("Flota inválida");
});

it("404 muestra mensaje y link al lobby; desmontar desconecta", async () => {
  m.getGame.mockRejectedValue(new ApiError({ status: 404, code: "NOT_FOUND", detail: "no existe" }));
  const { unmount } = render(<GameScreen id={9} />);
  expect(await screen.findByRole("alert")).toBeInTheDocument();
  expect(screen.getByRole("link", { name: /lobby/i })).toHaveAttribute("href", "#/");
  unmount();
  expect(m.disconnect).toHaveBeenCalled();
});

it("sin rival muestra mensaje de espera", async () => {
  m.getGame.mockResolvedValue(view({ status: "CREATED", me: { ships_placed: false }, opponent: null }));
  render(<GameScreen id={1} />);
  expect(await screen.findByText(/esperando.*rival/i)).toBeInTheDocument();
});

const fire = () => view({ turn: 2, me: { salvos: [{ turn: 1, cells: ["J10"], hits: [] }] }, opponent: {} });
const enemy = () => within(screen.getByRole("group", { name: /rival/i }));

it("FIRE: 5 celdas habilitan Disparar y llama fireSalvo; la 6.ª y las ya disparadas no cuentan", async () => {
  m.getGame.mockResolvedValue(fire());
  m.fireSalvo.mockResolvedValue(view({ turn: 2, me: { salvos: [{ turn: 2, cells: [], hits: [] }] } }));
  render(<GameScreen id={1} />);
  const btn = await screen.findByRole("button", { name: /disparar \(0\/5\)/i });
  expect(btn).toBeDisabled();
  fireEvent.click(enemy().getByRole("button", { name: "J10" }));
  for (const c of ["A1", "A2", "A3", "A4", "A5", "A6"]) fireEvent.click(enemy().getByRole("button", { name: c }));
  expect(enemy().getByRole("button", { name: "A6" })).not.toHaveClass("selected");
  const go = screen.getByRole("button", { name: /disparar \(5\/5\)/i });
  fireEvent.click(go);
  await waitFor(() => expect(m.fireSalvo).toHaveBeenCalledWith(1, ["A1", "A2", "A3", "A4", "A5"]));
  expect(await screen.findByText(/esperando.*rival/i)).toBeInTheDocument();
});

it("error de salvo muestra el detalle", async () => {
  m.getGame.mockResolvedValue(fire());
  m.fireSalvo.mockRejectedValue(new ApiError({ status: 409, code: "SALVO_ALREADY_FIRED", detail: "Ya disparaste" }));
  render(<GameScreen id={1} />);
  await screen.findByRole("button", { name: /disparar/i });
  for (const c of ["A1", "A2", "A3", "A4", "A5"]) fireEvent.click(enemy().getByRole("button", { name: c }));
  fireEvent.click(screen.getByRole("button", { name: /disparar \(5\/5\)/i }));
  expect(await screen.findByRole("alert")).toHaveTextContent("Ya disparaste");
});

it("error de /user/queue/errors es visible", async () => {
  m.getGame.mockResolvedValue(fire());
  render(<GameScreen id={1} />);
  await screen.findByRole("button", { name: /disparar/i });
  act(() => handlers.onError({ type: "", title: "", status: 400, code: "X", detail: "Emote inválido", instance: "" }));
  expect(screen.getByRole("alert")).toHaveTextContent("Emote inválido");
});

it("celda pendiente se marca y tablero rival se deshabilita esperando al rival", async () => {
  m.getGame.mockResolvedValue(view({ turn: 2, me: { salvos: [{ turn: 2, cells: ["A1"], hits: [] }] }, opponent: {} }));
  render(<GameScreen id={1} />);
  expect(await screen.findByText(/esperando.*salvo/i)).toBeInTheDocument();
  expect(enemy().getByRole("button", { name: "A1" })).toHaveClass("pending");
  expect(enemy().getByRole("button", { name: "A1" })).toBeDisabled();
});

it("FINISH con 5 hundidos del rival muestra Ganaste", async () => {
  const sunk = ["CARRIER", "BATTLESHIP", "SUBMARINE", "DESTROYER", "PATROL_BOAT"] as const;
  m.getGame.mockResolvedValue(view({ status: "FINISH", turn: 3, opponent: { sunk: [...sunk] } }));
  render(<GameScreen id={1} />);
  expect(await screen.findByText("Ganaste")).toBeInTheDocument();
  expect(screen.getByRole("link", { name: /lobby/i })).toHaveAttribute("href", "#/");
});

it("emotes: envía el propio y muestra solo los conocidos del rival", async () => {
  m.getGame.mockResolvedValue(fire());
  render(<GameScreen id={1} />);
  fireEvent.click(await screen.findByRole("button", { name: /emote 2/i }));
  expect(m.sendEmote).toHaveBeenCalledWith("emote-2");
  act(() => handlers.onEvent({ type: "EMOTE", game_id: 1, player_id: 2, emote: "evil.png" }));
  expect(screen.queryByRole("img", { name: /emote/i })).toBeNull();
  act(() => handlers.onEvent({ type: "EMOTE", game_id: 1, player_id: 2, emote: "emote-3" }));
  expect(screen.getByRole("img", { name: /emote 3/i })).toHaveAttribute("src", "/assets/emotes/emote-3.png");
});
