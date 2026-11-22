import { useCallback, useEffect, useRef, useState } from "react";
import { fireSalvo, getGame, placeShips } from "../api/games";
import { connectGame } from "../api/stomp";
import type { GameView, Nation, ShipPlacement, ShipType } from "../api/types";
import Board, { type Drop } from "../components/Board";
import EmoteBar, { EmoteBubble, useTransient } from "../components/EmoteBar";
import ShipPicker from "../components/ShipPicker";
import { enemyBoard, myBoard, type CellState } from "../domain/board";
import { canPlace, isFleetComplete, randomFleet, shipCells, type Orientation } from "../domain/fleet";
import { outcome, phase } from "../domain/phase";
import { SALVO_SIZE, toggleTarget } from "../domain/salvo";

const RESULT = { WON: ["Ganaste", "victory"], LOST: ["Perdiste", "defeat"], TIED: ["Empate", "defeat"] } as const;

const REFRESH_MS = 15_000;

const message =(e: unknown) => (e instanceof Error ? e.message : "Error inesperado");

function Plate({ name, nation }: { name: string; nation: Nation }) {
  return (
    <span className="player" data-nation={nation.toLowerCase()}>
      <svg viewBox="0 0 48 48" aria-hidden="true"><use href={`/assets/sprite.svg#emb-${nation.toLowerCase()}`} /></svg>
      {name}
    </span>
  );
}

export default function GameScreen({ id }: { id: number }) {
  const [view, setView] = useState<GameView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [placed, setPlaced] = useState<ShipPlacement[]>([]);
  const [selected, setSelected] = useState<ShipType | null>(null);
  const [orientation, setOrientation] = useState<Orientation>("H");
  const [drag, setDrag] = useState<ShipType | null>(null);
  const [drop, setDrop] = useState<Drop | null>(null);

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
    // refresco periódico con guarda anti-solape; se omite con la partida terminada
    let inFlight = false;
    const timer = setInterval(() => {
      if (inFlight || viewRef.current?.status === "FINISH") return;
      inFlight = true;
      void load().finally(() => { inFlight = false; });
    }, REFRESH_MS);
    return () => {
      clearInterval(timer);
      document.removeEventListener("visibilitychange", resync);
      window.removeEventListener("online", resync);
      conn.disconnect();
    };
  }, [id, load, setEmote]);

  const sceneNation = view?.nation;
  useEffect(() => {
    if (sceneNation) document.body.dataset.nation = sceneNation.toLowerCase();
    return () => { delete document.body.dataset.nation; };
  }, [sceneNation]);

  const rotate = () => setOrientation((o) => (o === "H" ? "V" : "H"));
  useEffect(() => {
    const key = (e: KeyboardEvent) => { if (e.key.toLowerCase() === "r" && !e.ctrlKey && !e.metaKey) rotate(); };
    document.addEventListener("keydown", key);
    return () => document.removeEventListener("keydown", key);
  }, []);

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
      <div className="game">
        {p === "FINISHED" ? (
          <div role="status">
            {res && <><img src={`/assets/img/${RESULT[res][1]}.png`} alt="" width={160} /><h2>{RESULT[res][0]}</h2></>}
            <a href="#/">Volver al lobby</a>
          </div>
        ) : (
          <p role="status">Turno {v.turn}{p === "WAITING_OPPONENT_SALVO" && ": esperando el salvo del rival…"}</p>
        )}
        <h3>
          <Plate name={v.me.player.name} nation={v.me.player.nation ?? v.nation} />{" "}
          {v.me.sunk.length > 0 && `(hundidos: ${v.me.sunk.length})`}
        </h3>
        <Board label="Tu tablero" cells={myBoard(v)} ships={v.me.ships} nation={v.me.player.nation ?? v.nation} disabled />
        <h3>
          <Plate name={v.opponent?.player.name ?? "Rival"} nation={v.opponent?.player.nation ?? v.nation} />{" "}
          {(v.opponent?.sunk.length ?? 0) > 0 && `(hundidos: ${v.opponent?.sunk.length})`}
          <EmoteBubble emote={emote} name={v.opponent?.player.name ?? "rival"} />
        </h3>
        <Board label="Tablero rival" cells={enemy} nation={v.opponent?.player.nation ?? v.nation} disabled={p !== "FIRE"}
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

  const place = (type: ShipType, origin: string) => {
    const sc = shipCells(type, origin, orientation);
    if (!sc || !canPlace(placed, { type, cells: sc })) return;
    setPlaced([...placed, { type, cells: sc }]);
    setSelected(null);
  };

  const onCell = (c: string) => {
    const hit = placed.find((s) => s.cells.includes(c));
    if (hit) return setPlaced(placed.filter((s) => s !== hit));
    if (selected) place(selected, c);
  };

  // pointer events (no HTML5 DnD: no funciona en táctil). Con captura táctil el target es la pieza: se resuelve por coordenadas.
  const cellAt = (e: React.PointerEvent) => {
    const el = document.elementFromPoint?.(e.clientX, e.clientY) ?? e.target;
    return (el as Element | null)?.closest<HTMLElement>("[data-cell]")?.dataset.cell ?? null;
  };
  const onMove = (e: React.PointerEvent) => {
    const at = drag && cellAt(e);
    if (!drag || !at) return setDrop(null);
    const sc = shipCells(drag, at, orientation);
    setDrop({ at, cells: sc ?? [], valid: !!sc && canPlace(placed, { type: drag, cells: sc }) });
  };
  const endDrag = () => { setDrag(null); setDrop(null); };
  const onUp = (e: React.PointerEvent) => {
    const at = drag && cellAt(e);
    if (drag && at) place(drag, at);
    endDrag();
  };

  const send = () => {
    setError(null);
    placeShips(id, placed).then(setView, (err) => setError(message(err)));
  };

  return (
    <div className="game" onPointerMove={onMove} onPointerUp={onUp} onPointerCancel={endDrag}>
      <p>Coloca tus barcos: arrástralos al tablero, o elige uno y pulsa la celda de origen. Gira con ⟳, R, la rueda o doble toque. Pulsa un barco colocado para quitarlo.</p>
      <ShipPicker placed={placed.map((s) => s.type)} selected={selected} orientation={orientation}
        onSelect={setSelected} onRotate={rotate} onDragStart={(t) => { setSelected(t); setDrag(t); }} />
      <div onWheel={() => selected && rotate()} onContextMenu={(e) => { if (selected) { e.preventDefault(); rotate(); } }}>
        <Board cells={cells} ships={placed} nation={view.nation} drop={drop} onCellClick={onCell} />
      </div>
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={() => { setPlaced(randomFleet()); setSelected(null); }}>Al azar</button>
      <button type="button" disabled={!isFleetComplete(placed)} onClick={send}>Listo · enviar barcos</button>
    </div>
  );
}
