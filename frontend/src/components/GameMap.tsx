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
  const [scale, setScale] = useState(1);

  // Escala "cover": el mapa llena el contenedor conservando proporción; los marcadores se contraescalan (--s) para no cambiar de tamaño en pantalla.
  useLayoutEffect(() => {
    const el = scroller.current;
    if (!el) return;
    const fit = () => { const k = Math.max(el.clientWidth / MAP_W, el.clientHeight / MAP_H); if (k > 0) setScale(k); };
    fit();
    if (typeof ResizeObserver === "undefined") return;
    const ro = new ResizeObserver(fit);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  useLayoutEffect(() => {
    const el = scroller.current;
    if (!el) return;
    const c = initialCenter(nation, MAP_LOCATIONS);
    el.scrollLeft = c.x * scale - el.clientWidth / 2;
    el.scrollTop = c.y * scale - el.clientHeight / 2;
    // solo al cambiar de nación o al fijarse la escala inicial
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nation, scale > 0]);

  useEffect(() => { if (selected) sheet.current?.focus(); }, [selected]);

  const close = () => {
    const id = selected?.id;
    setSelected(null);
    if (id) spots.current.get(id)?.focus();
  };

  // Escape cierra el menú aunque el foco esté en una ubicación
  useEffect(() => {
    if (!selected) return;
    const esc = (e: KeyboardEvent) => { if (e.key === "Escape") close(); };
    window.addEventListener("keydown", esc);
    return () => window.removeEventListener("keydown", esc);
  });
  // Arrastre con ratón; el táctil ya desplaza nativo (touch-action: pan-x pan-y)
  const pan = useRef<{ x: number; y: number } | null>(null);
  const run = (fn: () => void) => { setSelected(null); fn(); };

  const info = selected ? locationState(selected.id, games, myId) : null;

  return (
    <>
      <div ref={scroller} className="map" style={{ overflow: "auto", maxWidth: "100%" }}
        onPointerDown={(e) => { if (e.pointerType === "mouse") pan.current = { x: e.clientX, y: e.clientY }; }}
        onPointerMove={(e) => {
          const el = scroller.current;
          if (!pan.current || !el) return;
          el.scrollLeft -= e.clientX - pan.current.x; el.scrollTop -= e.clientY - pan.current.y;
          pan.current = { x: e.clientX, y: e.clientY };
        }}
        onPointerUp={() => { pan.current = null; }} onPointerLeave={() => { pan.current = null; }}>
        <svg viewBox={`0 0 ${MAP_W} ${MAP_H}`} width={MAP_W * scale} height={MAP_H * scale} style={{ display: "block", maxWidth: "none", "--s": 1 / scale } as React.CSSProperties}>
          <image href="/assets/img/mapa-game-mobile.png" width={MAP_W} height={MAP_H} />
          {MAP_LOCATIONS.map((loc) => {
            const { state } = locationState(loc.id, games, myId);
            const open = () => { setSelected(loc); sheet.current?.focus(); };
            return (
              <g
                key={loc.id}
                ref={(n) => { if (n) spots.current.set(loc.id, n); else spots.current.delete(loc.id); }}
                className="map-spot"
                data-state={state}
                role="button"
                tabIndex={0}
                aria-label={`${loc.id}: ${STATE_LABEL[state]}`}
                onClick={open}
                onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); open(); } }}
              >
                <circle cx={loc.x} cy={loc.y} r={loc.r / scale} />
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
