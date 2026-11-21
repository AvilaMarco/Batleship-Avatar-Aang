import { expect, it } from "vitest";
import { parseRoute } from "./router";

it("parsea las rutas por hash", () => {
  expect(parseRoute("#/login")).toEqual({ name: "login" });
  expect(parseRoute("#/")).toEqual({ name: "lobby" });
  expect(parseRoute("#/games/12")).toEqual({ name: "game", id: 12 });
});

it("una ruta inválida cae en lobby", () => {
  expect(parseRoute("#/nada")).toEqual({ name: "lobby" });
  expect(parseRoute("#/games/abc")).toEqual({ name: "lobby" });
  expect(parseRoute("")).toEqual({ name: "lobby" });
});
