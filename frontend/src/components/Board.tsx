import type { Nation, ShipPlacement, ShipType } from "../api/types";
import { COLS, ROWS, type CellId } from "../domain/cell";
import type { CellState } from "../domain/board";

export interface Drop { at: CellId; cells: CellId[]; valid: boolean }

interface Props {
  cells: Record<CellId, CellState>;
  onCellClick?: (c: CellId) => void;
  disabled?: boolean;
  label?: string;
  /** Nación del dueño del tablero: color de barcos y marcadores. */
  nation?: Nation;
  ships?: ShipPlacement[];
  drop?: Drop | null;
}

const SPRITE = "/assets/sprite.svg#";
const SPRITE_ID: Record<ShipType, string> = {
  CARRIER: "carrier", BATTLESHIP: "battleship", DESTROYER: "cruiser", SUBMARINE: "submarine", PATROL_BOAT: "patrol",
};

// El color no es la única señal: símbolo (sprite) + texto en title.
const TEXT: Record<CellState, string> = {
  empty: "agua", ship: "barco", hit: "impacto", miss: "fallo", pending: "pendiente", selected: "seleccionada",
};

function Mark({ state, nation }: { state: CellState; nation: Nation }) {
  const id = { hit: `hit-${nation.toLowerCase()}`, miss: "miss", selected: "target", pending: "target" }[state as string];
  if (!id) return null;
  return <svg viewBox="0 0 40 40" aria-hidden="true" width="100%" height="100%"><use href={`${SPRITE}${id}`} /></svg>;
}

function ShipSprite({ ship }: { ship: ShipPlacement }) {
  const n = ship.cells.length;
  const [r, c] = [ROWS.indexOf(ship.cells[0][0]), Number(ship.cells[0].slice(1)) - 1];
  const vertical = n > 1 && ship.cells[1][0] !== ship.cells[0][0];
  const href = `${SPRITE}${SPRITE_ID[ship.type]}`;
  // fila/columna +2: la 1ª es la cabecera; position:absolute no altera el autoflujo de las celdas
  return (
    <svg aria-hidden="true" data-ship={ship.type} viewBox={vertical ? `0 0 40 ${n * 40}` : `0 0 ${n * 40} 40`} preserveAspectRatio="none"
      style={{ position: "absolute", inset: 0, width: "100%", height: "100%", pointerEvents: "none",
        gridColumn: `${c + 2} / span ${vertical ? 1 : n}`, gridRow: `${r + 2} / span ${vertical ? n : 1}` }}>
      {vertical
        ? <use href={href} width={n * 40} height={40} transform="translate(40 0) rotate(90)" />
        : <use href={href} width={n * 40} height={40} />}
    </svg>
  );
}

export default function Board({ cells, onCellClick, disabled, label = "Tablero", nation = "WATER", ships = [], drop }: Props) {
  const cols = Array.from({ length: COLS }, (_, i) => i + 1);
  return (
    <div role="group" aria-label={label} className="board"
      style={{ display: "grid", gridTemplateColumns: `repeat(${COLS + 1}, minmax(0, 1fr))`, maxWidth: 480,
        "--acc": `var(--n-mid)`, "--acc2": `var(--n-lite)` } as React.CSSProperties}
      data-nation={nation.toLowerCase()}>
      <span />
      {cols.map((n) => <span key={n} aria-hidden="true">{n}</span>)}
      {[...ROWS].map((r) => [
        <span key={r} aria-hidden="true">{r}</span>,
        ...cols.map((n) => {
          const id = `${r}${n}`;
          const state = cells[id] ?? "empty";
          const over = drop && (drop.at === id || drop.cells.includes(id));
          return (
            <button key={id} type="button" aria-label={id} data-cell={id} title={`${id}: ${TEXT[state]}`} className={`cell ${state}`}
              data-drop={over ? (drop.valid ? "valid" : "invalid") : undefined}
              disabled={disabled} onClick={() => onCellClick?.(id)} style={{ aspectRatio: "1", minWidth: 0 }}>
              <Mark state={state} nation={nation} />
            </button>
          );
        }),
      ])}
      {ships.map((s) => <ShipSprite key={s.type} ship={s} />)}
    </div>
  );
}
