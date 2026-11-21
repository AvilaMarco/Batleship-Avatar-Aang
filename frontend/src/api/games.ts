import { request } from "./http";
import type { GameSummary, GameView, Nation, ShipPlacement } from "./types";

export const listGames = () => request<GameSummary[]>("GET", "/api/games");
export const createGame = (nation: Nation, location: string) =>
  request<GameView>("POST", "/api/games", { nation, location });
export const joinGame = (id: number) => request<GameView>("POST", `/api/games/${id}/players`);
export const getGame = (id: number) => request<GameView>("GET", `/api/games/${id}`);
export const placeShips = (id: number, ships: ShipPlacement[]) =>
  request<GameView>("POST", `/api/games/${id}/ships`, { ships });
// Anexo F (v2.1, aún no implementado en backend)
export const fireSalvo = (id: number, cells: string[]) =>
  request<GameView>("POST", `/api/games/${id}/salvos`, { cells });
