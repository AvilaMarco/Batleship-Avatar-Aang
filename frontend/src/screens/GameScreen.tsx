import { useCallback, useEffect, useState } from "react";
import { getGame, placeShips } from "../api/games";
import { connectGame } from "../api/stomp";
import type { GameView, ShipPlacement, ShipType } from "../api/types";
import Board from "../components/Board";
import ShipPicker from "../components/ShipPicker";
import { myBoard, type CellState } from "../domain/board";
import { canPlace, isFleetComplete, shipCells, type Orientation } from "../domain/fleet";
import { phase } from "../domain/phase";

const message = (e: unknown) => (e instanceof Error ? e.message : "Error inesperado");

export default function GameScreen({ id }: { id: number }) {
  const [view, setView] = useState<GameView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [placed, setPlaced] = useState<ShipPlacement[]>([]);
  const [selected, setSelected] = useState<ShipType | null>(null);
  const [orientation, setOrientation] = useState<Orientation>("H");

  const load = useCallback(
    () => getGame(id).then(setView, (e) => setLoadError(message(e))),
    [id],
  );

  useEffect(() => {
    void load();
    const conn = connectGame(id, {
      onConnect: () => void load(),
      onEvent: (e) => { if (e.type !== "EMOTE") void load(); },
      onError: (p) => setError(p.detail),
    });
    return () => conn.disconnect();
  }, [id, load]);

  if (loadError) {
    return <div><p role="alert">{loadError}</p><a href="#/">Volver al lobby</a></div>;
  }
  if (!view) return <p>Cargando…</p>;

  const ph = phase(view);
  if (ph === "WAITING_OPPONENT") return <p>Esperando a un rival…</p>;
  if (ph === "WAITING_OPPONENT_SHIPS") return <p>Esperando a que el rival coloque sus barcos…</p>;
  if (ph !== "PLACE_SHIPS") return <Board cells={myBoard(view)} disabled />; // 507: disparos

  const cells: Record<string, CellState> = {};
  for (const s of placed) for (const c of s.cells) cells[c] = "ship";

  const onCell = (c: string) => {
    const hit = placed.find((s) => s.cells.includes(c));
    if (hit) return setPlaced(placed.filter((s) => s !== hit));
    if (!selected) return;
    const sc = shipCells(selected, c, orientation);
    if (sc && canPlace(placed, { type: selected, cells: sc })) {
      setPlaced([...placed, { type: selected, cells: sc }]);
      setSelected(null);
    }
  };

  const send = () => {
    setError(null);
    placeShips(id, placed).then(setView, (e) => setError(message(e)));
  };

  return (
    <div>
      <p>Coloca tus barcos: elige uno, rótalo y pulsa la celda de origen. Pulsa un barco colocado para quitarlo.</p>
      <ShipPicker placed={placed.map((s) => s.type)} selected={selected} orientation={orientation}
        onSelect={setSelected} onRotate={() => setOrientation(orientation === "H" ? "V" : "H")} />
      <Board cells={cells} onCellClick={onCell} />
      {error && <p role="alert">{error}</p>}
      <button type="button" disabled={!isFleetComplete(placed)} onClick={send}>Enviar barcos</button>
    </div>
  );
}
