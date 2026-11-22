import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, expect, it } from "vitest";
import "@testing-library/jest-dom/vitest";
import Board from "./Board";

afterEach(cleanup);
const SPRITE = "/assets/sprite.svg#";
const uses = (el: Element) => [...el.querySelectorAll("use")].map((u) => u.getAttribute("href"));

// Rojo hasta 808 (punto 1): marcadores con sprite.svg manteniendo accesibilidad.
it("impacto, fallo y objetivo se dibujan con <use> del sprite y conservan aria-label y texto", () => {
  render(<Board cells={{ A1: "hit", A2: "miss", A3: "selected" }} />);
  const hit = screen.getByRole("button", { name: "A1" });
  expect(uses(hit)).toHaveLength(1);
  expect(uses(hit)[0]).toMatch(new RegExp(`^${SPRITE}hit-(water|earth|fire|air|lotus)$`));
  expect(hit).toHaveAttribute("title", "A1: impacto");
  expect(uses(screen.getByRole("button", { name: "A2" }))).toEqual([`${SPRITE}miss`]);
  expect(uses(screen.getByRole("button", { name: "A3" }))).toEqual([`${SPRITE}target`]);
  expect(screen.getByRole("button", { name: "A2" })).toHaveAttribute("title", "A2: fallo");
});

it("el impacto usa el marcador de la nación del dueño del tablero", () => {
  render(<Board cells={{ A1: "hit" }} nation="FIRE" />);
  expect(uses(screen.getByRole("button", { name: "A1" }))).toEqual([`${SPRITE}hit-fire`]);
});
