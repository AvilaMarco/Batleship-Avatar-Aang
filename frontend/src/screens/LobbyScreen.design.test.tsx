import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import LobbyScreen from "./LobbyScreen";

vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({ profile: { id: 1, name: "Aang", email: "a@a.com", nation: "AIR", stats: {} }, refreshProfile: vi.fn(), logout: vi.fn() }),
}));

const ok = (b: unknown) => Promise.resolve(new Response(JSON.stringify(b), { status: 200 }));
beforeEach(() => {
  localStorage.clear();
  vi.stubGlobal("fetch", vi.fn(() => ok([])));
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const toggle = () => screen.getByRole("button", { name: /menú/i });

// Rojo hasta 808 (punto 3)
it("el menú flotante se oculta con la ficha (aria-expanded) y se recuerda en localStorage", () => {
  const first = render(<LobbyScreen />);
  expect(toggle()).toHaveAttribute("aria-expanded", "true");
  fireEvent.click(toggle());
  expect(toggle()).toHaveAttribute("aria-expanded", "false");
  expect(localStorage.length).toBeGreaterThan(0);
  first.unmount();
  render(<LobbyScreen />);
  expect(toggle()).toHaveAttribute("aria-expanded", "false");
});

// Contrato inventado (tarea 815): el menú del lobby enlaza al perfil.
it("el menú del lobby tiene un enlace al perfil (#/profile)", () => {
  render(<LobbyScreen />);
  expect(screen.getByRole("link", { name: /perfil/i })).toHaveAttribute("href", "#/profile");
});
