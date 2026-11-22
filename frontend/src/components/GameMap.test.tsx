import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import type { GameSummary, Nation } from "../api/types";
import GameMap from "./GameMap";

const game = (id: number, location: string, players: number[]): GameSummary => ({
  id, nation: "EARTH", location, status: "CREATED",
  players: players.map((p) => ({ id: p, name: `P${p}`, nation: "EARTH" })),
});

function setup(games: GameSummary[] = [], nation: Nation | null = "EARTH") {
  const cb = { onCreate: vi.fn(), onJoin: vi.fn(), onOpen: vi.fn() };
  const view = render(<GameMap games={games} myId={1} nation={nation} {...cb} />);
  return { ...view, ...cb };
}
const spot = (id: string) => screen.getByRole("button", { name: new RegExp(`^${id}:`) });

afterEach(cleanup);

it("dibuja un svg 1400x800 con el mapa de fondo", () => {
  const { container } = setup();
  const svg = container.querySelector("svg")!;
  expect(svg).toHaveAttribute("viewBox", "0 0 1400 800");
  expect(svg.querySelector("image")?.getAttribute("href")).toBe("/assets/map.svg");
});

it("hay un control por ubicación con su data-state", () => {
  setup([game(7, "E1", [2]), game(8, "E2", [1]), game(9, "E3", [2, 3])]);
  expect(spot("E1")).toHaveAttribute("data-state", "join");
  expect(spot("E2")).toHaveAttribute("data-state", "mine");
  expect(spot("E3")).toHaveAttribute("data-state", "full");
  expect(spot("E4")).toHaveAttribute("data-state", "empty");
  expect(spot("E4")).toHaveAttribute("tabindex", "0");
});

it("tocar una ubicación libre abre el menú y no crea hasta pulsar la acción", () => {
  const { onCreate } = setup();
  fireEvent.click(spot("E4"));
  expect(onCreate).not.toHaveBeenCalled();
  const menu = screen.getByRole("dialog");
  fireEvent.click(within(menu).getByRole("button", { name: "Crear partida en E4" }));
  expect(onCreate).toHaveBeenCalledWith(expect.objectContaining({ nation: "EARTH", id: "E4" }));
});

it("partida ajena de 1 jugador ofrece Unirse", () => {
  const { onJoin } = setup([game(7, "E1", [2])]);
  fireEvent.click(spot("E1"));
  fireEvent.click(screen.getByRole("button", { name: "Unirse a la partida en E1" }));
  expect(onJoin).toHaveBeenCalledWith(7);
});

it("partida propia ofrece Volver", () => {
  const { onOpen } = setup([game(8, "E2", [1])]);
  fireEvent.click(spot("E2"));
  fireEvent.click(screen.getByRole("button", { name: "Volver a la partida en E2" }));
  expect(onOpen).toHaveBeenCalledWith(8);
});

it("partida llena muestra el aviso y ninguna acción", () => {
  setup([game(9, "E3", [2, 3])]);
  fireEvent.click(spot("E3"));
  const menu = screen.getByRole("dialog");
  expect(within(menu).getByText(/partida llena/i)).toBeInTheDocument();
  for (const b of within(menu).queryAllByRole("button", { name: /crear|unirse|volver/i })) expect(b).toBeDisabled();
});

it("Escape cierra el menú", () => {
  setup();
  fireEvent.click(spot("E4"));
  fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });
  expect(screen.queryByRole("dialog")).toBeNull();
});

it("el botón Cerrar cierra el menú", () => {
  setup();
  fireEvent.click(spot("E4"));
  fireEvent.click(screen.getByRole("button", { name: "Cerrar" }));
  expect(screen.queryByRole("dialog")).toBeNull();
});

it.each([["Enter", "Enter"], ["Espacio", " "]])("el control responde a %s", (_n, key) => {
  setup();
  fireEvent.keyDown(spot("E4"), { key });
  expect(screen.getByRole("dialog")).toBeInTheDocument();
});

it("el contenedor arranca centrado en la nación del jugador", () => {
  const scrollOf = (n: Nation) => {
    const { container, unmount } = setup([], n);
    let el: HTMLElement | null = container.querySelector("svg")!.parentElement;
    let left = 0;
    for (; el; el = el.parentElement) left = Math.max(left, el.scrollLeft);
    unmount();
    return left;
  };
  expect(scrollOf("FIRE")).toBeLessThan(scrollOf("EARTH"));
});
