import { describe, expect, it } from "vitest";
import { isCell, ROWS, COLS } from "./cell";

describe("isCell", () => {
  it("acepta A1..J10", () => {
    expect(ROWS).toBe("ABCDEFGHIJ");
    expect(COLS).toBe(10);
    for (const c of ["A1", "J10", "C5", "B9"]) expect(isCell(c)).toBe(true);
  });
  it("rechaza lo demás", () => {
    for (const c of ["K1", "A0", "A11", "a1", "A01", "", "A", "AA1"]) expect(isCell(c)).toBe(false);
  });
});
