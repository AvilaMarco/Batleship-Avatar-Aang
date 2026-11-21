import { describe, expect, it } from "vitest";
import { SALVO_SIZE, toggleTarget } from "./salvo";

describe("toggleTarget", () => {
  it("agrega", () => expect(toggleTarget(["A1"], "A2", [])).toEqual(["A1", "A2"]));
  it("quita una seleccionada", () => expect(toggleTarget(["A1", "A2"], "A1", [])).toEqual(["A2"]));
  it("ignora la 6.ª", () => {
    const five = ["A1", "A2", "A3", "A4", "A5"];
    expect(SALVO_SIZE).toBe(5);
    expect(toggleTarget(five, "B1", [])).toEqual(five);
  });
  it("ignora celda ya disparada", () => expect(toggleTarget([], "A1", ["A1"])).toEqual([]));
  it("no muta la entrada", () => {
    const s = ["A1"]; toggleTarget(s, "A2", []); expect(s).toEqual(["A1"]);
  });
});
