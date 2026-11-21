import { describe, expect, it } from "vitest";
import { enemyBoard, myBoard } from "./board";
import { view } from "./fixtures";

describe("enemyBoard", () => {
  it("pending / hit / miss", () => {
    const v = view({
      turn: 2,
      me: { salvos: [{ turn: 1, cells: ["A1", "A2"], hits: ["A1"] }, { turn: 2, cells: ["B1"], hits: [] }] },
    });
    expect(enemyBoard(v)).toEqual({ A1: "hit", A2: "miss", B1: "pending" });
  });
  it("en FINISH el último turno ya está resuelto", () => {
    const v = view({ status: "FINISH", turn: 1, me: { salvos: [{ turn: 1, cells: ["A1", "A2"], hits: ["A1"] }] } });
    expect(enemyBoard(v)).toEqual({ A1: "hit", A2: "miss" });
  });
});

describe("myBoard", () => {
  it("barcos, impactos y fallos del rival; no revela el turno sin resolver", () => {
    const v = view({
      turn: 2,
      me: { ships: [{ type: "PATROL_BOAT", cells: ["A1", "A2"] }] },
      opponent: { salvos: [{ turn: 1, cells: ["A1", "C3"], hits: ["A1"] }, { turn: 2, cells: ["A2"], hits: [] }] },
    });
    expect(myBoard(v)).toEqual({ A1: "hit", A2: "ship", C3: "miss" });
  });
  it("sin rival", () => {
    expect(myBoard(view({ opponent: null, me: { ships: [{ type: "PATROL_BOAT", cells: ["A1"] }] } }))).toEqual({ A1: "ship" });
  });
});
