import { useCallback, useEffect, useRef, useState } from "react";
import { fireSalvo, getGame, placeShips } from "../api/games";
import { connectGame } from "../api/stomp";
import type { GameView, ShipPlacement, ShipType } from "../api/types";
import Board from "../components/Board";
import EmoteBar, { EmoteBubble, useTransient } from "../components/EmoteBar";
import ShipPicker from "../components/ShipPicker";
import { enemyBoard, myBoard, type CellState } from "../domain/board";
import { canPlace, isFleetComplete, shipCells, type Orientation } from "../domain/fleet";
import { outcome, phase } from "../domain/phase";
import { SALVO_SIZE, toggleTarget } from "../domain/salvo";

const RESULT = { WON: ["Ganaste", "victory"], LOST: ["Perdiste", "defeat"], TIED: ["Empate", "defeat"] } as const;

const message = (e: unknown) => (e instanceof Error ? e.message : "Error inesperado");

export default function GameScreen({ id }: { id: number }) {
  const [view, setView] = useState<GameView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [placed, setPlaced] = useState<ShipPlacement[]>([]);
  const [selected, setSelected] = useState<ShipType | null>(null);
  const [orientation, setOrientation] = useState<Orientation>("H");

  const [targets, setTargets] = useState<string[]>([]);
  const [emote, setEmote] = useTransient<string>();
  const [sendEmote, setSendEmote] = useState<((e: string) => void) | null>(null);

  const load = useCallback(
    () => getGame(id).then(setView, (e) => setLoadError(message(e))),
    [id],
  );

  const viewRef = useRef<GameView | null>(null);
  viewRef.current = view;
  useEffect(() => {
    void load();
    const conn = connectGame(id, {
      onConnect: () => void load(),
      onEvent: (e) => {
        if (e.type !== "EMOTE") return void load();
        if (e.player_id !== viewRef.current?.me.player.id) setEmote(e.emote);
      },
      onError: (p) => setError(p.detail),
    });
    setSendEmote(() => conn.sendEmote);
    // respaldo si se pierde un evento (móvil dormido, red caída): recargar al volver visible u online
    const resync = () => { if (document.visibilityState === "visible") void load(); };
    document.addEventListener("visibilitychange", resync);
    window.addEventListener("online", resync);
    return () => {
      document.removeEventListener("visibilitychange", resync);
      window.removeEventListener("online", resync);
      conn.disconnect();
    };
  }, [id, load, setEmote]);

  if (loadError) {
    return <div><p role="alert">{loadError}</p><a href="#/">Volver al lobby</a></div>;
  }
  if (!view) return <p>Cargando…</p>;

  const ph = phase(view);
  if (ph === "WAITING_OPPONENT") return <p>Esperando a un rival…</p>;
  if (ph === "WAITING_OPPONENT_SHIPS") return <p>Esperando a que el rival coloque sus barcos…</p>;
  if (ph !== "PLACE_SHIPS") return playing(view, ph);

  function playing(v: GameView, p: typeof ph) {
    const fired = v.me.salvos.flatMap((x) => x.cells);
    const enemy: Record<string, CellState> = enemyBoard(v);
    for (const c of targets) enemy[c] = "selected";
    const res = outcome(v);
    const shoot = () => {
      setError(null);
      fireSalvo(id, targets).then((nv) => { setTargets([]); setView(nv); }, (e) => setError(message(e)));
    };
    return (
      <div>
        {p === "FINISHED" ? (
          <div role="status">
            {res && <><img src={`/assets/img/${RESULT[res][1]}.png`} alt="" width={160} /><h2>{RESULT[res][0]}</h2></>}
            <a href="#/">Volver al lobby</a>
          </div>
        ) : (
          <p role="status">Turno {v.turn}{p === "WAITING_OPPONENT_SALVO" && ": esperando el salvo del rival…"}</p>
        )}
        <h3>Tu flota {v.me.sunk.length > 0 && `(hundidos: ${v.me.sunk.length})`}</h3>
        <Board label="Tu tablero" cells={myBoard(v)} disabled />
        <h3>
          {v.opponent?.player.name ?? "Rival"} {(v.opponent?.sunk.length ?? 0) > 0 && `(hundidos: ${v.opponent?.sunk.length})`}
          <EmoteBubble emote={emote} name={v.opponent?.player.name ?? "rival"} />
        </h3>
        <Board label="Tablero rival" cells={enemy} disabled={p !== "FIRE"}
          onCellClick={(c) => setTargets(toggleTarget(targets, c, fired))} />
        {error && <p role="alert">{error}</p>}
        {p === "FIRE" && (
          <button type="button" disabled={targets.length !== SALVO_SIZE} onClick={shoot}>
            Disparar ({targets.length}/{SALVO_SIZE})
          </button>
        )}
        {sendEmote && <EmoteBar onSend={sendEmote} />}
      </div>
    );
  }

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
