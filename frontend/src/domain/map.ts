import type { GameSummary, Nation } from "../api/types";
import { MAP_H, MAP_W, type MapLocation } from "./map-locations";

export type SpotState = "empty" | "join" | "mine" | "full";
export type SpotInfo = { state: "empty" } | { state: Exclude<SpotState, "empty">; game: GameSummary };

export function locationState(id: string, games: GameSummary[], myId: number): SpotInfo {
  const game = games.find((g) => g.location === id);
  if (!game) return { state: "empty" };
  if (game.players.some((p) => p.id === myId)) return { state: "mine", game };
  return { state: game.players.length === 1 ? "join" : "full", game };
}

/** Centro (coords del mapa) sobre el que arranca el scroll: media de la nación, o el centro del mapa. */
export function initialCenter(nation: Nation | null, locations: MapLocation[]): { x: number; y: number } {
  const own = locations.filter((l) => l.nation === nation);
  if (nation === null || own.length === 0) return { x: MAP_W / 2, y: MAP_H / 2 };
  return { x: own.reduce((s, l) => s + l.x, 0) / own.length, y: own.reduce((s, l) => s + l.y, 0) / own.length };
}
