import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import LobbyScreen from "./LobbyScreen";

const auth = vi.hoisted(() => ({
  profile: { id: 1, name: "Aang", email: "a@a.com", nation: "AIR", stats: {} } as Record<string, unknown>,
  refreshProfile: vi.fn(),
  logout: vi.fn(),
}));
vi.mock("../auth/AuthContext", () => ({ useAuth: () => auth }));

const json = (status: number, body: unknown) =>
  Promise.resolve(new Response(JSON.stringify(body), { status }));
const stats = { score: 3, won: 1, tied: 0, lost: 2, win_rate: 0.3 };
const ranking = [{ player: { id: 9, name: "Zuko", nation: "FIRE" }, stats }];
const game = (id: number, location: string, players: number[]) => ({
  id, nation: "EARTH", location, status: "CREATED",
  players: players.map((p) => ({ id: p, name: `P${p}`, nation: "EARTH" })),
});

function stub(games: unknown[], extra: (m: string, u: string) => Promise<Response> | undefined = () => undefined) {
  const f = vi.fn((u: string, init?: RequestInit) => {
    const m = init?.method ?? "GET";
    return extra(m, u) ?? (u === "/api/games" && m === "GET" ? json(200, games)
      : u === "/api/players/ranking" ? json(200, ranking) : json(200, {}));
  });
  vi.stubGlobal("fetch", f);
  return f;
}

beforeEach(() => {
  location.hash = "#/";
  auth.profile.nation = "AIR";
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

it("sin nación muestra el selector y llama PATCH", async () => {
  auth.profile.nation = null;
  const f = stub([]);
  render(<LobbyScreen />);
  fireEvent.click(screen.getByRole("button", { name: /fuego/i }));
  await waitFor(() => expect(auth.refreshProfile).toHaveBeenCalled());
  const call = f.mock.calls.find(([, i]) => i?.method === "PATCH")!;
  expect(call[0]).toBe("/api/players/me");
  expect(JSON.parse(call[1]!.body as string)).toEqual({ nation: "FIRE" });
});

it("partida ajena con 1 jugador ofrece Unirse y navega", async () => {
  stub([game(7, "E1", [2])], (m, u) => (m === "POST" && u === "/api/games/7/players" ? json(200, { id: 7 }) : undefined));
  render(<LobbyScreen />);
  fireEvent.click(await screen.findByRole("button", { name: /^E1:/ }));
  fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /unirse.*E1/i }));
  await waitFor(() => expect(location.hash).toBe("#/games/7"));
});

it("partida propia ofrece Volver", async () => {
  stub([game(8, "E2", [1])]);
  render(<LobbyScreen />);
  fireEvent.click(await screen.findByRole("button", { name: /^E2:/ }));
  fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /volver.*E2/i }));
  expect(location.hash).toBe("#/games/8");
});

it("punto libre crea la partida y navega", async () => {
  const f = stub([], (m, u) => (m === "POST" && u === "/api/games" ? json(201, { id: 5 }) : undefined));
  render(<LobbyScreen />);
  fireEvent.click(await screen.findByRole("button", { name: /^W1:/ }));
  fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /crear.*W1/i }));
  await waitFor(() => expect(location.hash).toBe("#/games/5"));
  const call = f.mock.calls.find(([, i]) => i?.method === "POST")!;
  expect(JSON.parse(call[1]!.body as string)).toEqual({ nation: "WATER", location: "W1" });
});

it("LOCATION_TAKEN muestra error y recarga", async () => {
  const f = stub([], (m, u) =>
    m === "POST" && u === "/api/games" ? json(409, { status: 409, code: "LOCATION_TAKEN", detail: "x" }) : undefined);
  render(<LobbyScreen />);
  fireEvent.click(await screen.findByRole("button", { name: /^W1:/ }));
  fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /crear.*W1/i }));
  expect(await screen.findByRole("alert")).toHaveTextContent(/ocupad/i);
  await waitFor(() => expect(f.mock.calls.filter(([u, i]) => u === "/api/games" && i?.method === "GET").length).toBeGreaterThan(1));
});

it("muestra el ranking y Salir llama logout", async () => {
  stub([]);
  render(<LobbyScreen />);
  expect(await screen.findByText("Zuko")).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Salir" }));
  expect(auth.logout).toHaveBeenCalled();
});
