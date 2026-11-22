import type { ShipPlacement, ShipType } from "../api/types";
import { COLS, ROWS, type CellId } from "./cell";

export type Orientation = "H" | "V";
export const SHIP_SIZES: Record<ShipType, number> = {
  CARRIER: 5, BATTLESHIP: 4, SUBMARINE: 3, DESTROYER: 3, PATROL_BOAT: 2,
};

export function shipCells(type: ShipType, origin: CellId, o: Orientation): CellId[] | null {
  const row = ROWS.indexOf(origin[0]);
  const col = Number(origin.slice(1)) - 1;
  const cells: CellId[] = [];
  for (let i = 0; i < SHIP_SIZES[type]; i++) {
    const r = o === "V" ? row + i : row;
    const c = o === "H" ? col + i : col;
    if (r < 0 || r >= ROWS.length || c < 0 || c >= COLS) return null;
    cells.push(`${ROWS[r]}${c + 1}`);
  }
  return cells;
}

// Solo guía al usuario; la validación real es la del backend (ADR 004 A.4).
export function canPlace(placed: ShipPlacement[], ship: ShipPlacement): boolean {
  if (placed.some((p) => p.type === ship.type)) return false;
  const used = new Set(placed.flatMap((p) => p.cells));
  return !ship.cells.some((c) => used.has(c));
}

export const isFleetComplete = (placed: ShipPlacement[]): boolean =>
  (Object.keys(SHIP_SIZES) as ShipType[]).every((t) => placed.some((p) => p.type === t));

/** Flota aleatoria válida. `rng` inyectable para tests. Reintenta por barco hasta colocarlo (siempre cabe en 10×10). */
export function randomFleet(rng: () => number = Math.random): ShipPlacement[] {
  const placed: ShipPlacement[] = [];
  for (const type of Object.keys(SHIP_SIZES) as ShipType[]) {
    for (let tries = 0; ; tries++) {
      // con rng constante el sorteo no varía: se avanza el origen por intentos para no colgarse
      const k = Math.floor(rng() * 100 + tries) % 100;
      const o: Orientation = rng() < 0.5 ? "H" : "V";
      const cells = shipCells(type, `${ROWS[Math.floor(k / COLS)]}${(k % COLS) + 1}`, o);
      if (cells && canPlace(placed, { type, cells })) { placed.push({ type, cells }); break; }
      if (tries > 2000) return randomFleet(Math.random);
    }
  }
  return placed;
}
