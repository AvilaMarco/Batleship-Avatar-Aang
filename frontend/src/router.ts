import { useEffect, useState } from "react";

export type Route = { name: "login" } | { name: "lobby" } | { name: "game"; id: number };

export function parseRoute(hash: string): Route {
  if (hash === "#/login") return { name: "login" };
  const m = /^#\/games\/(\d+)$/.exec(hash);
  if (m) return { name: "game", id: Number(m[1]) };
  return { name: "lobby" };
}

export const navigate = (hash: string): void => {
  location.hash = hash;
};

export function useHashRoute(): Route {
  const [hash, setHash] = useState(location.hash);
  useEffect(() => {
    const on = () => setHash(location.hash);
    window.addEventListener("hashchange", on);
    return () => window.removeEventListener("hashchange", on);
  }, []);
  return parseRoute(hash);
}
