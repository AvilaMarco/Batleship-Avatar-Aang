import type { GameView, SalvoRecord } from "../api/types";
import type { CellId } from "./cell";

export type CellState = "empty" | "ship" | "hit" | "miss" | "pending";
type Board = Record<CellId, CellState>;

// Un salvo del turno actual está sin resolver (el turno avanza al resolverse), salvo en FINISH.
const resolved = (v: GameView, s: SalvoRecord) => v.status === "FINISH" || s.turn < v.turn;

function paint(board: Board, v: GameView, salvos: SalvoRecord[], unresolved: CellState | null) {
  for (const s of salvos) {
    if (!resolved(v, s)) {
      if (unresolved) for (const c of s.cells) board[c] = unresolved;
      continue;
    }
    for (const c of s.cells) board[c] = s.hits.includes(c) ? "hit" : "miss";
  }
}

export function myBoard(v: GameView): Board {
  const b: Board = {};
  for (const ship of v.me.ships) for (const c of ship.cells) b[c] = "ship";
  paint(b, v, v.opponent?.salvos ?? [], null); // no se revela lo que el rival aún no resolvió
  return b;
}

export function enemyBoard(v: GameView): Board {
  const b: Board = {};
  paint(b, v, v.me.salvos, "pending");
  return b;
}
