import { COLS, ROWS, type CellId } from "../domain/cell";
import type { CellState } from "../domain/board";

interface Props {
  cells: Record<CellId, CellState>;
  onCellClick?: (c: CellId) => void;
  disabled?: boolean;
  label?: string;
}

// El color no es la única señal: símbolo + texto en title.
const MARK: Record<CellState, [string, string]> = {
  empty: ["", "agua"], ship: ["▣", "barco"], hit: ["X", "impacto"],
  miss: ["•", "fallo"], pending: ["?", "pendiente"], selected: ["+", "seleccionada"],
};

export default function Board({ cells, onCellClick, disabled, label = "Tablero" }: Props) {
  const cols = Array.from({ length: COLS }, (_, i) => i + 1);
  return (
    <div role="group" aria-label={label} className="board" style={{ display: "grid", gridTemplateColumns: `repeat(${COLS + 1}, minmax(0, 1fr))`, maxWidth: 480 }}>
      <span />
      {cols.map((n) => <span key={n} aria-hidden="true">{n}</span>)}
      {[...ROWS].map((r) => [
        <span key={r} aria-hidden="true">{r}</span>,
        ...cols.map((n) => {
          const id = `${r}${n}`;
          const state = cells[id] ?? "empty";
          return (
            <button key={id} type="button" aria-label={id} title={`${id}: ${MARK[state][1]}`} className={`cell ${state}`}
              disabled={disabled} onClick={() => onCellClick?.(id)} style={{ aspectRatio: "1", minWidth: 0 }}>
              {MARK[state][0]}
            </button>
          );
        }),
      ])}
    </div>
  );
}
