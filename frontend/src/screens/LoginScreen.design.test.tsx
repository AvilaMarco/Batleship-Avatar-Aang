import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, expect, it } from "vitest";
import "@testing-library/jest-dom/vitest";
import { AuthProvider } from "../auth/AuthContext";
import LoginScreen from "./LoginScreen";

afterEach(cleanup);

// Rojo hasta 808 (punto 5): sin texto de relleno, silueta + audio VTT, miniaturas.
it("no hay encabezado de relleno; hay audio con pistas VTT y botón silenciar", () => {
  const { container } = render(<AuthProvider><LoginScreen /></AuthProvider>);
  expect(screen.queryByRole("heading")).toBeNull();
  expect(container.querySelector('audio track[kind="metadata"]')).not.toBeNull();
  expect(screen.getByRole("button", { name: /silenciar/i })).toHaveAttribute("aria-pressed");
});

it("hay una miniatura por nación y solo la activa va marcada", () => {
  const { container } = render(<AuthProvider><LoginScreen /></AuthProvider>);
  for (const n of ["agua", "tierra", "fuego", "aire"]) {
    expect(screen.getByRole("button", { name: new RegExp(n, "i") })).toBeInTheDocument();
  }
  expect(container.querySelectorAll('[aria-current="true"]')).toHaveLength(1);
});
