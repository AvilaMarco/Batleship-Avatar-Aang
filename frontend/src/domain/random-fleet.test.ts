import { expect, it } from "vitest";
import { isFleetComplete, randomFleet } from "./fleet";

it("randomFleet devuelve una flota completa sin solapes", () => {
  for (let i = 0; i < 50; i++) {
    const f = randomFleet();
    expect(isFleetComplete(f)).toBe(true);
    const cells = f.flatMap((s) => s.cells);
    expect(new Set(cells).size).toBe(17);
  }
});

it("randomFleet es determinista con un rng fijo", () => {
  expect(randomFleet(() => 0)).toEqual(randomFleet(() => 0));
});
