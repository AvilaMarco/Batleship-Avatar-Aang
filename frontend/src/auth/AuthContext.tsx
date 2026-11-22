import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { clearToken, getToken } from "../api/auth";
import * as players from "../api/players";
import type { Nation, PlayerProfile } from "../api/types";
import { navigate } from "../router";

interface Auth {
  profile: PlayerProfile | null;
  /** false mientras se valida el token guardado al montar */
  ready: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string, nation?: Nation) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}

const Ctx = createContext<Auth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [profile, setProfile] = useState<PlayerProfile | null>(null);
  const [ready, setReady] = useState(!getToken());

  const refreshProfile = useCallback(async () => setProfile(await players.getMe()), []);

  useEffect(() => {
    if (!getToken()) {
      navigate("#/login");
      return;
    }
    // 401 ya limpia token y navega a login dentro de request()
    players.getMe().then(setProfile, () => {}).finally(() => setReady(true));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    await players.login(email, password);
    await refreshProfile();
    navigate("#/");
  }, [refreshProfile]);

  const register = useCallback(async (name: string, email: string, password: string, nation?: Nation) => {
    await players.register(name, email, password);
    await players.login(email, password);
    if (nation) await players.setNation(nation);
    await refreshProfile();
    navigate("#/");
  }, [refreshProfile]);

  const logout = useCallback(() => {
    clearToken();
    setProfile(null);
    navigate("#/login");
  }, []);

  const value = useMemo(
    () => ({ profile, ready, login, register, logout, refreshProfile }),
    [profile, ready, login, register, logout, refreshProfile],
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): Auth {
  const v = useContext(Ctx);
  if (!v) throw new Error("useAuth fuera de AuthProvider");
  return v;
}
