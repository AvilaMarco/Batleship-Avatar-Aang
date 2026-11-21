import type { CellId } from "./cell";

export const SALVO_SIZE = 5;

export function toggleTarget(selected: CellId[], cell: CellId, alreadyFired: CellId[]): CellId[] {
  if (alreadyFired.includes(cell)) return selected;
  if (selected.includes(cell)) return selected.filter((c) => c !== cell);
  return selected.length >= SALVO_SIZE ? selected : [...selected, cell];
}
