import { cleanup, render } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import { afterEach, expect, it } from "vitest";
import Board from "./Board";

afterEach(cleanup);
const patrol = { type: "PATROL_BOAT" as const, cells: ["A1", "A2"] };

it("un barco hundido se marca con el sprite sunk en cada casilla", () => {
  const { container } = render(<Board cells={{}} ships={[patrol]} sunk={["PATROL_BOAT"]} />);
  const ship = container.querySelector('[data-ship="PATROL_BOAT"]')!;
  expect(ship).toHaveAttribute("data-sunk", "true");
  expect([...ship.querySelectorAll("use")].filter((u) => u.getAttribute("href")?.endsWith("#sunk"))).toHaveLength(2);
});

it("un barco a flote no lleva marca sunk", () => {
  const { container } = render(<Board cells={{}} ships={[patrol]} sunk={[]} />);
  expect(container.querySelector("[data-sunk]")).toBeNull();
});
