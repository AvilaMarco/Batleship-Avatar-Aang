import { COLS, ROWS, type CellId } from "../domain/cell";
import type { CellState } from "../domain/board";

interface Props {
  cells: Record<CellId, CellState>;
  onCellClick?: (c: CellId) => void;
  disabled?: boolean;
}

export default function Board({ cells, onCellClick, disabled }: Props) {
  const cols = Array.from({ length: COLS }, (_, i) => i + 1);
  return (
    <div role="group" aria-label="Tablero" style={{ display: "grid", gridTemplateColumns: `repeat(${COLS + 1}, minmax(0, 1fr))`, maxWidth: 480 }}>
      <span />
      {cols.map((n) => <span key={n} aria-hidden="true">{n}</span>)}
      {[...ROWS].map((r) => [
        <span key={r} aria-hidden="true">{r}</span>,
        ...cols.map((n) => {
          const id = `${r}${n}`;
          const state = cells[id] ?? "empty";
          return (
            <button key={id} type="button" aria-label={id} className={state} disabled={disabled}
              onClick={() => onCellClick?.(id)} style={{ aspectRatio: "1", minWidth: 0 }} />
          );
        }),
      ])}
    </div>
  );
}
