import { describe, expect, it } from "vitest";
import { canPlace, isFleetComplete, shipCells, SHIP_SIZES } from "./fleet";
import type { ShipPlacement, ShipType } from "../api/types";

describe("shipCells", () => {
  it("horizontal y vertical", () => {
    expect(shipCells("PATROL_BOAT", "A1", "H")).toEqual(["A1", "A2"]);
    expect(shipCells("DESTROYER", "B3", "V")).toEqual(["B3", "C3", "D3"]);
    expect(shipCells("CARRIER", "J6", "H")).toEqual(["J6", "J7", "J8", "J9", "J10"]);
  });
  it("null en borde derecho e inferior", () => {
    expect(shipCells("CARRIER", "A7", "H")).toBeNull();
    expect(shipCells("BATTLESHIP", "H1", "V")).toBeNull();
  });
  it("tamaños", () => {
    expect(SHIP_SIZES).toEqual({ CARRIER: 5, BATTLESHIP: 4, SUBMARINE: 3, DESTROYER: 3, PATROL_BOAT: 2 });
  });
});

describe("canPlace", () => {
  const a: ShipPlacement = { type: "PATROL_BOAT", cells: ["A1", "A2"] };
  it("acepta barco libre", () => {
    expect(canPlace([a], { type: "DESTROYER", cells: ["B1", "B2", "B3"] })).toBe(true);
  });
  it("rechaza solape", () => {
    expect(canPlace([a], { type: "DESTROYER", cells: ["A2", "A3", "A4"] })).toBe(false);
  });
  it("rechaza tipo repetido", () => {
    expect(canPlace([a], { type: "PATROL_BOAT", cells: ["C1", "C2"] })).toBe(false);
  });
});

describe("isFleetComplete", () => {
  const all: ShipType[] = ["CARRIER", "BATTLESHIP", "SUBMARINE", "DESTROYER", "PATROL_BOAT"];
  const fleet = all.map((type, i) => ({ type, cells: [`A${i + 1}`] }));
  it("true con los 5 tipos", () => expect(isFleetComplete(fleet)).toBe(true));
  it("false si falta uno", () => expect(isFleetComplete(fleet.slice(1))).toBe(false));
});
