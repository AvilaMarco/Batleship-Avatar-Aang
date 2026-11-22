import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../api/http";
import * as gamesApi from "../api/games";
import * as players from "../api/players";
import type { GameSummary, Nation, RankingEntry } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import GameMap from "../components/GameMap";
import NationPicker from "../components/NationPicker";
import Ranking from "../components/Ranking";
import { navigate } from "../router";

const MENU_KEY = "shipbender.lobbyMenu";

const MSG: Record<string, string> = {
  LOCATION_TAKEN: "Esa ubicación ya está ocupada.",
  GAME_FULL: "La partida ya está llena.",
  ALREADY_IN_GAME: "Ya estás en una partida.",
};

export default function LobbyScreen() {
  const { profile, refreshProfile, logout } = useAuth();
  const [games, setGames] = useState<GameSummary[]>([]);
  const [ranking, setRanking] = useState<RankingEntry[]>([]);
  const [error, setError] = useState("");
  const [menuOpen, setMenuOpen] = useState(() => localStorage.getItem(MENU_KEY) !== "closed");
  const toggleMenu = () => {
    localStorage.setItem(MENU_KEY, menuOpen ? "closed" : "open");
    setMenuOpen(!menuOpen);
  };

  const fail = (e: unknown) =>
    setError(e instanceof ApiError ? MSG[e.code] ?? "No se pudo completar la operación." : "No se pudo conectar con el servidor.");

  const reload = useCallback(
    () => gamesApi.listGames().then(setGames, fail),
    [],
  );

  useEffect(() => {
    void reload();
    players.getRanking().then(setRanking, fail);
  }, [reload]);

  async function act(fn: () => Promise<{ id: number }>) {
    setError("");
    try {
      navigate(`#/games/${(await fn()).id}`);
    } catch (e) {
      fail(e);
      void reload();
    }
  }

  async function pick(n: Nation) {
    setError("");
    try {
      await players.setNation(n);
      await refreshProfile();
    } catch (e) {
      fail(e);
    }
  }

  if (!profile) return null;
  return (
    <>
      <button type="button" className="menu-toggle" aria-expanded={menuOpen} aria-controls="lobby-menu" onClick={toggleMenu}>Menú</button>
      <div id="lobby-menu" className="lobby-menu" hidden={!menuOpen}>
        <button type="button" onClick={logout}>Salir</button>
        {profile.nation !== null && <button type="button" onClick={() => void reload()}>Actualizar</button>}
        {profile.nation !== null && <Ranking entries={ranking} />}
      </div>
      {error && <p role="alert">{error}</p>}
      {profile.nation === null ? (
        <NationPicker onPick={pick} />
      ) : (
        <>
          <GameMap
            games={games}
            myId={profile.id}
            nation={profile.nation}
            onCreate={(l) => act(() => gamesApi.createGame(l.nation, l.id))}
            onJoin={(id) => act(() => gamesApi.joinGame(id))}
            onOpen={(id) => navigate(`#/games/${id}`)}
          />
        </>
      )}
    </>
  );
}
