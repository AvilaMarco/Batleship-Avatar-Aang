import type { GameSummary, Nation } from "../api/types";
import { MAP_LOCATIONS, type MapLocation } from "../domain/map-locations";

// tamaño natural de fondo-mapa.png; los puntos se posicionan en % para escalar en móvil
const W = 1395;
const H = 950;

interface Props {
  games: GameSummary[];
  myId: number;
  nation?: Nation | null; // se usa desde la 803
  onCreate: (loc: MapLocation) => void;
  onJoin: (gameId: number) => void;
  onOpen: (gameId: number) => void;
}

export default function GameMap({ games, myId, onCreate, onJoin, onOpen }: Props) {
  return (
    <div style={{ position: "relative", width: "100%", maxWidth: W }}>
      <img src="/assets/img/fondo-mapa.png" alt="Mapa de ubicaciones" style={{ width: "100%", display: "block" }} />
      {MAP_LOCATIONS.map((loc) => {
        const g = games.find((x) => x.location === loc.id);
        const mine = g?.players.some((p) => p.id === myId);
        let label = `Crear partida en ${loc.id}`;
        let action = () => onCreate(loc);
        if (g && mine) { label = `Volver a la partida en ${loc.id}`; action = () => onOpen(g.id); }
        else if (g && g.players.length === 1) { label = `Unirse a la partida en ${loc.id}`; action = () => onJoin(g.id); }
        const occupied = g && !mine && g.players.length !== 1;
        if (occupied) label = `Partida llena en ${loc.id}`;
        return (
          <button
            key={loc.id}
            type="button"
            aria-label={label}
            title={g ? `${label}: ${g.players.map((p) => p.name).join(" vs ")}` : label}
            disabled={occupied}
            onClick={action}
            style={{
              position: "absolute",
              left: `${((loc.x - loc.r) / W) * 100}%`,
              top: `${((loc.y - loc.r) / H) * 100}%`,
              width: `${((loc.r * 2) / W) * 100}%`,
              aspectRatio: "1",
              minWidth: 24,
              borderRadius: "50%",
              padding: 0,
              opacity: g ? 1 : 0.6,
            }}
          >
            {g ? g.players.length : "+"}
          </button>
        );
      })}
    </div>
  );
}
