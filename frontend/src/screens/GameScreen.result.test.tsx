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
