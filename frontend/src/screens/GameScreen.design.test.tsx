import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import GameScreen from "./GameScreen";
import { view } from "../domain/fixtures";

const m = vi.hoisted(() => ({ getGame: vi.fn(), placeShips: vi.fn(), fireSalvo: vi.fn() }));
vi.mock("../api/games", () => m);
vi.mock("../api/stomp", () => ({ connectGame: () => ({ sendEmote: vi.fn(), disconnect: vi.fn() }) }));

const toPlace = () => view({ status: "CREATED", me: { ships_placed: false }, opponent: {} });
const ships = [
  { type: "CARRIER", cells: ["A1", "A2", "A3", "A4", "A5"] },
  { type: "BATTLESHIP", cells: ["B1", "B2", "B3", "B4"] },
  { type: "SUBMARINE", cells: ["C1", "C2", "C3"] },
  { type: "DESTROYER", cells: ["D1", "D2", "D3"] },
  { type: "PATROL_BOAT", cells: ["E1", "E2"] },
] as const;
const uses = (el: Element) => [...el.querySelectorAll("use")].map((u) => u.getAttribute("href"));

beforeEach(() => m.getGame.mockResolvedValue(toPlace()));
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  delete document.body.dataset.nation;
});

// --- Rojo hasta 808 (punto 2) ---
it("la escena lleva .game y data-nation del body = nación de la ubicación", async () => {
  m.getGame.mockResolvedValue(view({ nation: "EARTH", me: { ships: [...ships] }, opponent: {} }));
  const { container } = render(<GameScreen id={1} />);
  await screen.findByRole("group", { name: /rival/i });
  expect(container.querySelector(".game")).not.toBeNull();
  expect(document.body.dataset.nation).toBe("earth");
});

it("hay una placa .player por jugador con el emblema de su nación", async () => {
  m.getGame.mockResolvedValue(view({
    me: { player: { id: 1, name: "Aang", nation: "AIR" }, ships: [...ships] },
    opponent: { player: { id: 2, name: "Zuko", nation: "FIRE" } },
  }));
  const { container } = render(<GameScreen id={1} />);
  await screen.findByRole("group", { name: /rival/i });
  const plates = container.querySelectorAll(".player");
  expect(plates).toHaveLength(2);
  expect(plates[0]).toHaveTextContent("Aang");
  expect(uses(plates[0])).toEqual(["/assets/sprite.svg#emb-air"]);
  expect(plates[1]).toHaveTextContent("Zuko");
  expect(uses(plates[1])).toEqual(["/assets/sprite.svg#emb-fire"]);
});

// --- Rojo hasta 808 (punto 1) ---
it("mi tablero dibuja cada barco con su sprite", async () => {
  m.getGame.mockResolvedValue(view({ me: { ships: [...ships] }, opponent: {} }));
  render(<GameScreen id={1} />);
  const mine = await screen.findByRole("group", { name: "Tu tablero" });
  const ids = uses(mine).map((h) => h?.split("#")[1]);
  for (const s of ["carrier", "battleship", "cruiser", "submarine", "patrol"]) expect(ids).toContain(s);
});

// --- Rojo hasta 808 (punto 4): colocar flota. Contrato asumido: data-drop="valid|invalid" en la celda bajo el puntero. ---
const piece = (n: RegExp) => screen.findByRole("button", { name: n });
const cell = (id: string) => screen.getByRole("button", { name: id });

it("arrastrar un barco (pointer events) a una celda lo coloca", async () => {
  render(<GameScreen id={1} />);
  fireEvent.pointerDown(await piece(/patrullero/i));
  fireEvent.pointerMove(cell("A1"));
  fireEvent.pointerUp(cell("A1"));
  expect(cell("A1")).toHaveClass("ship");
  expect(cell("A2")).toHaveClass("ship");
});

it("mientras se arrastra, la celda marca drop valid / invalid", async () => {
  render(<GameScreen id={1} />);
  fireEvent.pointerDown(await piece(/portaaviones/i));
  fireEvent.pointerMove(cell("A1"));
  expect(cell("A1")).toHaveAttribute("data-drop", "valid");
  fireEvent.pointerMove(cell("A8")); // se sale del tablero en horizontal
  expect(cell("A8")).toHaveAttribute("data-drop", "invalid");
});

it("la tecla R gira el barco seleccionado (vertical)", async () => {
  render(<GameScreen id={1} />);
  fireEvent.click(await piece(/patrullero/i));
  fireEvent.keyDown(document, { key: "r" });
  fireEvent.click(cell("A1"));
  expect(cell("B1")).toHaveClass("ship");
  expect(cell("A2")).not.toHaveClass("ship");
});

it("la rueda sobre el tablero gira el barco seleccionado", async () => {
  render(<GameScreen id={1} />);
  fireEvent.click(await piece(/patrullero/i));
  fireEvent.wheel(screen.getByRole("group", { name: /tablero/i }), { deltaY: 100 });
  fireEvent.click(cell("A1"));
  expect(cell("B1")).toHaveClass("ship");
});

it("doble toque sobre el barco lo gira", async () => {
  render(<GameScreen id={1} />);
  const p = await piece(/patrullero/i);
  fireEvent.click(p);
  fireEvent.doubleClick(p);
  fireEvent.click(cell("A1"));
  expect(cell("B1")).toHaveClass("ship");
});

it("Al azar coloca los 5 barcos y Listo envía la flota", async () => {
  m.placeShips.mockResolvedValue(view({ me: { ships_placed: true }, opponent: { ships_placed: false } }));
  render(<GameScreen id={1} />);
  const ready = await screen.findByRole("button", { name: /^listo/i });
  expect(ready).toBeDisabled();
  fireEvent.click(screen.getByRole("button", { name: /al azar/i }));
  expect(ready).toBeEnabled();
  fireEvent.click(ready);
  await waitFor(() => expect(m.placeShips).toHaveBeenCalledTimes(1));
  expect(m.placeShips.mock.calls[0][1]).toHaveLength(5);
});
