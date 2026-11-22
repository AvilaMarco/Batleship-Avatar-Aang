import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import App from "./App";
import { clearToken, setToken } from "./api/auth";

const m = vi.hoisted(() => ({ getMe: vi.fn(), login: vi.fn(), register: vi.fn() }));
vi.mock("./api/players", () => ({ getMe: m.getMe, login: m.login, register: m.register }));
vi.mock("./screens/LobbyScreen", () => ({ default: () => <p>LOBBY</p> }));
vi.mock("./screens/GameScreen", () => ({ default: ({ id }: { id: number }) => <p>GAME {id}</p> }));

const profile = { id: 1, name: "Aang", email: "a@b.c", nation: null, stats: {} };

beforeEach(() => {
  clearToken();
  m.getMe.mockResolvedValue(profile);
});
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  location.hash = "";
});

it("muestra el título", () => {
  render(<App />);
  expect(screen.getByRole("heading", { name: "The Last Shipbender" })).toBeInTheDocument();
});

it("con sesión iniciada, #/login lleva a #/ y muestra el lobby (rojo hasta 807)", async () => {
  setToken("t");
  location.hash = "#/login";
  render(<App />);
  expect(await screen.findByText("LOBBY")).toBeInTheDocument();
  expect(location.hash).toBe("#/");
  expect(screen.queryByText("Ya has iniciado sesión.")).toBeNull();
});

it("login correcto desde #/login termina en #/", async () => {
  location.hash = "#/login";
  m.login.mockResolvedValue({});
  render(<App />);
  fireEvent.change(await screen.findByLabelText("Email"), { target: { value: "a@b.c" } });
  fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "secreto123" } });
  fireEvent.click(screen.getByRole("button", { name: "Entrar" }));
  await waitFor(() => expect(location.hash).toBe("#/"));
  expect(await screen.findByText("LOBBY")).toBeInTheDocument();
});

it.each(["#/", "#/games/5"])("sin perfil y ruta %s sigue yendo a #/login", async (hash) => {
  location.hash = hash;
  render(<App />);
  expect(await screen.findByRole("form", { name: "Iniciar sesión" })).toBeInTheDocument();
  expect(location.hash).toBe("#/login");
});

it("mientras ready es falso no redirige ni muestra el login", async () => {
  setToken("t");
  m.getMe.mockReturnValue(new Promise(() => {}));
  location.hash = "#/games/5";
  render(<App />);
  expect(screen.getByText("Cargando…")).toBeInTheDocument();
  expect(location.hash).toBe("#/games/5");
  expect(screen.queryByRole("form", { name: "Iniciar sesión" })).toBeNull();
});
