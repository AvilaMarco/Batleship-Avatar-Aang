import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import { view } from "../domain/fixtures";

const m = vi.hoisted(() => ({ getGame: vi.fn() }));
vi.mock("../api/games", async (orig) => ({ ...(await orig<object>()), getGame: m.getGame }));
vi.mock("../api/socket", () => ({ connectGame: () => ({ close: vi.fn() }) }));
import GameScreen from "./GameScreen";

afterEach(cleanup);
const five = ["CARRIER", "BATTLESHIP", "DESTROYER", "SUBMARINE", "PATROL_BOAT"] as const;

it("la pantalla final lleva la clase result con el desenlace", async () => {
  m.getGame.mockResolvedValue(view({ status: "FINISH", opponent: { sunk: [...five] }, me: { sunk: ["CARRIER"] } }));
  render(<GameScreen id={1} />);
  const box = await waitFor(() => screen.getByRole("status"));
  expect(box).toHaveClass("result");
  expect(box).toHaveAttribute("data-outcome", "WON");
});

it.each([
  ["LOST", { sunk: [] as never[] }, { sunk: [...five] }, "Perdiste"],
  ["TIED", { sunk: [...five] }, { sunk: [...five] }, "Empate"],
])("la pantalla final %s muestra su título, imagen y acciones", async (outcome, opponent, me, title) => {
  m.getGame.mockResolvedValue(view({ status: "FINISH", opponent, me }));
  render(<GameScreen id={1} />);
  const box = await waitFor(() => screen.getByRole("status"));
  expect(box).toHaveAttribute("data-outcome", outcome);
  expect(box.querySelector("h2")).toHaveTextContent(title);
  expect(box.querySelector("img")).not.toBeNull();
  expect(box.querySelector(".result-actions a")).toHaveAttribute("href", "#/");
});
