import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import GameScreen from "./GameScreen";
import { ApiError } from "../api/http";
import { view } from "../domain/fixtures";
import type { GameHandlers } from "../api/stomp";

const m = vi.hoisted(() => ({ getGame: vi.fn(), placeShips: vi.fn(), connectGame: vi.fn(), disconnect: vi.fn() }));
vi.mock("../api/games", () => ({ getGame: m.getGame, placeShips: m.placeShips }));
vi.mock("../api/stomp", () => ({ connectGame: m.connectGame }));

let handlers: GameHandlers;
const toPlace = () => view({ status: "CREATED", me: { ships_placed: false }, opponent: {} });

beforeEach(() => {
  m.getGame.mockResolvedValue(toPlace());
  m.connectGame.mockImplementation((_id: number, h: GameHandlers) => {
    handlers = h;
    return { sendEmote: vi.fn(), disconnect: m.disconnect };
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
