import type { GameView } from "../api/types";
import { SHIP_SIZES } from "./fleet";

export type Phase =
  | "WAITING_OPPONENT" | "PLACE_SHIPS" | "WAITING_OPPONENT_SHIPS"
  | "FIRE" | "WAITING_OPPONENT_SALVO" | "FINISHED";

export function phase(v: GameView): Phase {
  if (v.status === "FINISH") return "FINISHED";
  if (!v.opponent) return "WAITING_OPPONENT";
  if (!v.me.ships_placed) return "PLACE_SHIPS";
  if (!v.opponent.ships_placed) return "WAITING_OPPONENT_SHIPS";
  return v.me.salvos.some((s) => s.turn === v.turn) ? "WAITING_OPPONENT_SALVO" : "FIRE";
}

export function outcome(v: GameView): "WON" | "LOST" | "TIED" | null {
  if (v.status !== "FINISH") return null;
  const total = Object.keys(SHIP_SIZES).length;
  const mine = v.me.sunk.length >= total;
  const theirs = (v.opponent?.sunk.length ?? 0) >= total;
  if (mine && theirs) return "TIED";
  if (theirs) return "WON";
  return mine ? "LOST" : null; // FINISH sin flota hundida (p. ej. seed): sin resultado derivable
}
