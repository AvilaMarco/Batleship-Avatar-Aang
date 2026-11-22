import { useEffect, useLayoutEffect, useRef, useState } from "react";
import type { GameSummary, Nation } from "../api/types";
import { initialCenter, locationState } from "../domain/map";
import { MAP_H, MAP_LOCATIONS, MAP_W, type MapLocation } from "../domain/map-locations";

interface Props {
  games: GameSummary[];
  myId: number;
  nation: Nation | null;
  onCreate: (loc: MapLocation) => void;
  onJoin: (gameId: number) => void;
  onOpen: (gameId: number) => void;
}

const STATE_LABEL = { empty: "libre", join: "con un jugador", mine: "tu partida", full: "llena" } as const;

export default function GameMap({ games, myId, nation, onCreate, onJoin, onOpen }: Props) {
  const [selected, setSelected] = useState<MapLocation | null>(null);
  const scroller = useRef<HTMLDivElement>(null);
  const sheet = useRef<HTMLDivElement>(null);
  const spots = useRef(new Map<string, SVGGElement>());

  useLayoutEffect(() => {
    const el = scroller.current;
    if (!el) return;
    const c = initialCenter(nation, MAP_LOCATIONS);
    el.scrollLeft = c.x - el.clientWidth / 2;
    el.scrollTop = c.y - el.clientHeight / 2;
  }, [nation]);

  useEffect(() => { if (selected) sheet.current?.focus(); }, [selected]);

  const close = () => {
    const id = selected?.id;
    setSelected(null);
    if (id) spots.current.get(id)?.focus();
  };
  const run = (fn: () => void) => { setSelected(null); fn(); };

  const info = selected ? locationState(selected.id, games, myId) : null;

  return (
    <>
      <div ref={scroller} className="map" style={{ overflow: "auto", maxWidth: "100%" }}>
        <svg viewBox={`0 0 ${MAP_W} ${MAP_H}`} width={MAP_W} height={MAP_H} style={{ display: "block", maxWidth: "none" }}>
          <image href="/assets/img/mapa-game-mobile.png" width={MAP_W} height={MAP_H} />
          {MAP_LOCATIONS.map((loc) => {
            const { state } = locationState(loc.id, games, myId);
            const open = () => setSelected(loc);
            return (
              <g
                key={loc.id}
                ref={(n) => { if (n) spots.current.set(loc.id, n); else spots.current.delete(loc.id); }}
                className="map-spot"
                data-state={state}
                role="button"
                tabIndex={0}
                aria-label={`${loc.id}: ${state}`}
                onClick={open}
                onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); open(); } }}
              >
                <circle cx={loc.x} cy={loc.y} r={loc.r} />
              </g>
            );
          })}
        </svg>
      </div>
      {selected && info && (
        <div
          ref={sheet}
          className="map-sheet"
          role="dialog"
          aria-label={`Ubicación ${selected.id}: ${STATE_LABEL[info.state]}`}
          tabIndex={-1}
          onKeyDown={(e) => { if (e.key === "Escape") close(); }}
        >
          {info.state === "empty" && (
            <button type="button" onClick={() => run(() => onCreate(selected))}>Crear partida en {selected.id}</button>
          )}
          {info.state === "join" && (
            <button type="button" onClick={() => run(() => onJoin(info.game.id))}>Unirse a la partida en {selected.id}</button>
          )}
          {info.state === "mine" && (
            <button type="button" onClick={() => run(() => onOpen(info.game.id))}>Volver a la partida en {selected.id}</button>
          )}
          {info.state === "full" && <p>Partida llena en {selected.id}</p>}
          <button type="button" onClick={close}>Cerrar</button>
        </div>
      )}
    </>
  );
}
