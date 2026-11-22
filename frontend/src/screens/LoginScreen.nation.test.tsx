import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, beforeEach, expect, it } from "vitest";
import "@testing-library/jest-dom/vitest";
import { AuthProvider } from "../auth/AuthContext";
import LoginScreen from "./LoginScreen";

beforeEach(() => { sessionStorage.clear(); location.hash = "#/login"; });
afterEach(cleanup);

const setup = () => render(<AuthProvider><LoginScreen /></AuthProvider>);
const register = () => fireEvent.click(screen.getByRole("button", { name: "Registrarse" }));
const choice = (c: HTMLElement) => c.querySelector(".nation-choice") as HTMLElement;

it("entrar no muestra el selector de nación", () => {
  const { container } = setup();
  expect(choice(container)).toBeNull();
});

it("el registro ofrece 5 naciones (incluido Loto Blanco) con emblema emb-*", () => {
  const { container } = setup();
  register();
  const buttons = within(choice(container)).getAllByRole("button");
  expect(buttons.map((b) => b.getAttribute("aria-label"))).toEqual(["Agua", "Tierra", "Fuego", "Aire", "Loto Blanco"]);
  expect(buttons.map((b) => b.querySelector("use")?.getAttribute("href"))).toEqual(
    ["water", "earth", "fire", "air", "lotus"].map((n) => `/assets/sprite.svg#emb-${n}`),
  );
});

it("elegir una nación la marca con aria-pressed y desmarca la anterior", () => {
  const { container } = setup();
  register();
  const c = choice(container);
  expect(within(c).queryAllByRole("button", { pressed: true })).toHaveLength(0);
  fireEvent.click(within(c).getByRole("button", { name: "Loto Blanco" }));
  fireEvent.click(within(c).getByRole("button", { name: "Fuego" }));
  expect(within(c).getAllByRole("button", { pressed: true }).map((b) => b.getAttribute("aria-label"))).toEqual(["Fuego"]);
});
