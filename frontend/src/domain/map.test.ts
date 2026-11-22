import { describe, expect, it } from "vitest";
import type { GameSummary } from "../api/types";
import { MAP_H, MAP_W, type MapLocation } from "./map-locations";
import { initialCenter, locationState } from "./map";

const game = (id: number, location: string, players: number[]): GameSummary => ({
  id, nation: "EARTH", location, status: "CREATED",
  players: players.map((p) => ({ id: p, name: `P${p}`, nation: "EARTH" })),
});
const loc = (id: string, nation: MapLocation["nation"], x: number, y: number): MapLocation => ({ id, nation, x, y, r: 30 });

describe("locationState", () => {
  it("sin partida en la ubicación es empty", () => {
    expect(locationState("E1", [game(1, "E2", [2])], 1)).toEqual({ state: "empty" });
  });
  it("partida ajena de 1 jugador es join", () => {
    const g = game(7, "E1", [2]);
    expect(locationState("E1", [g], 1)).toEqual({ state: "join", game: g });
  });
  it("partida donde estoy es mine", () => {
    const g = game(8, "E1", [1]);
    expect(locationState("E1", [g], 1)).toEqual({ state: "mine", game: g });
  });
  it("partida de 2 donde estoy sigue siendo mine", () => {
    expect(locationState("E1", [game(9, "E1", [2, 1])], 1).state).toBe("mine");
  });
  it("partida de 2 donde no estoy es full", () => {
    expect(locationState("E1", [game(9, "E1", [2, 3])], 1).state).toBe("full");
  });
});

describe("initialCenter", () => {
  const locs = [loc("E1", "EARTH", 100, 200), loc("E2", "EARTH", 300, 400), loc("F1", "FIRE", 1000, 700)];
  it("es la media de las ubicaciones de la nación", () => {
    expect(initialCenter("EARTH", locs)).toEqual({ x: 200, y: 300 });
  });
  it("sin nación es el centro del mapa", () => {
    expect(initialCenter(null, locs)).toEqual({ x: MAP_W / 2, y: MAP_H / 2 });
  });
});

it("el mapa mide 1400x800", () => {
  expect([MAP_W, MAP_H]).toEqual([1400, 800]);
});
