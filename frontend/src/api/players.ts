import { setToken } from "./auth";
import { request } from "./http";
import type { LoginResponse, Nation, PlayerProfile, RankingEntry } from "./types";

export async function login(email: string, password: string): Promise<LoginResponse> {
  const r = await request<LoginResponse>("POST", "/api/auth/login", { email, password });
  setToken(r.token);
  return r;
}
export const register = (name: string, email: string, password: string) =>
  request<PlayerProfile>("POST", "/api/players", { name, email, password });
export const getMe = () => request<PlayerProfile>("GET", "/api/players/me");
export const setNation = (nation: Nation) => request<PlayerProfile>("PATCH", "/api/players/me", { nation });
export const getRanking = () => request<RankingEntry[]>("GET", "/api/players/ranking");
