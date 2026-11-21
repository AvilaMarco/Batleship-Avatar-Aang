import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import "@testing-library/jest-dom/vitest";
import { AuthProvider } from "../auth/AuthContext";
import LoginScreen from "./LoginScreen";

const json = (status: number, body: unknown) =>
  Promise.resolve(new Response(JSON.stringify(body), { status }));
const profile = { id: 1, name: "Aang", email: "a@a.com" };

beforeEach(() => {
  sessionStorage.clear();
  localStorage.clear();
  location.hash = "#/login";
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const setup = () =>
  render(
    <AuthProvider>
      <LoginScreen />
    </AuthProvider>,
  );

it("login OK navega a #/", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn((url: string) =>
      url === "/api/auth/login" ? json(200, { token: "tok" }) : json(200, profile),
    ),
  );
  setup();
  fireEvent.change(screen.getByLabelText("Email"), { target: { value: "a@a.com" } });
  fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "secreto1" } });
  fireEvent.click(screen.getByRole("button", { name: "Entrar" }));
  await waitFor(() => expect(location.hash).toBe("#/"));
});

it("401 muestra el error de credenciales", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(() => json(401, { status: 401, code: "INVALID_CREDENTIALS", detail: "x" })),
  );
  setup();
  fireEvent.change(screen.getByLabelText("Email"), { target: { value: "a@a.com" } });
  fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "mala" } });
  fireEvent.click(screen.getByRole("button", { name: "Entrar" }));
  expect(await screen.findByRole("alert")).toHaveTextContent(/credenciales/i);
});

it("registro con 409 muestra el error de email", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(() => json(409, { status: 409, code: "EMAIL_IN_USE", detail: "x" })),
  );
  setup();
  fireEvent.click(screen.getByRole("button", { name: "Registrarse" }));
  fireEvent.change(screen.getByLabelText("Nombre"), { target: { value: "Aang" } });
  fireEvent.change(screen.getByLabelText("Email"), { target: { value: "a@a.com" } });
  fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "secreto1" } });
  fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));
  expect(await screen.findByRole("alert")).toHaveTextContent(/email/i);
});

it("VALIDATION_FAILED muestra errores por campo", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(() =>
      json(400, { status: 400, code: "VALIDATION_FAILED", detail: "x", errors: { password: ["demasiado corta"] } }),
    ),
  );
  setup();
  fireEvent.click(screen.getByRole("button", { name: "Registrarse" }));
  fireEvent.change(screen.getByLabelText("Nombre"), { target: { value: "Aang" } });
  fireEvent.change(screen.getByLabelText("Email"), { target: { value: "a@a.com" } });
  fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "1" } });
  fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));
  expect(await screen.findByRole("alert")).toHaveTextContent("demasiado corta");
});
