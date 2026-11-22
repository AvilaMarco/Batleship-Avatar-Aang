import { expect, test, type Page } from "@playwright/test";

const SHIPS = ["Portaaviones", "Acorazado", "Submarino", "Destructor", "Patrullero"];
// A: filas A-E horizontal desde la columna 1; B: columnas 1-5 vertical desde la fila A.
const fleetA = ["A1", "B1", "C1", "D1", "E1"];
const fleetB = ["A1", "A2", "A3", "A4", "A5"];
const cellsOfB = ["A1", "B1", "C1", "D1", "E1", "A2", "B2", "C2", "D2", "A3", "B3", "C3", "A4", "B4", "C4", "A5", "B5"];
const water = ["J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8", "J9", "J10", "I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "I10"];

async function register(page: Page, name: string) {
  await page.goto("/#/login");
  await page.getByRole("button", { name: "Registrarse" }).click();
  await page.getByLabel("Nombre").fill(name);
  await page.getByLabel("Email").fill(`${name}@e2e.test`);
  await page.getByLabel("Contraseña").fill("secreto123");
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  await page.getByRole("button", { name: "Agua" }).click();
}

async function placeFleet(page: Page, origins: string[], vertical: boolean) {
  if (vertical) await page.getByRole("button", { name: /^Rotar/ }).click();
  const board = page.getByRole("group", { name: "Tablero", exact: true });
  for (const [i, ship] of SHIPS.entries()) {
    await page.getByRole("button", { name: new RegExp(`^${ship}`) }).click();
    await board.getByRole("button", { name: origins[i], exact: true }).click();
  }
  await page.getByRole("button", { name: "Enviar barcos" }).click();
}

async function fire(page: Page, cells: string[]) {
  const enemy = page.getByRole("group", { name: "Tablero rival" });
  for (const c of cells) await enemy.getByRole("button", { name: c, exact: true }).click();
  await page.getByRole("button", { name: /^Disparar \(5\/5\)/ }).click();
}

test("partida completa: A hunde la flota de B en 4 turnos", async ({ browser }) => {
  const id = Date.now();
  const a = await (await browser.newContext()).newPage();
  const b = await (await browser.newContext()).newPage();

  await register(a, `a${id}`);
  // dos pasos: pulsar la ubicación libre y luego "Crear partida en <id>"
  const free = a.getByRole("button", { name: /: empty$/ }).first();
  const loc = (await free.getAttribute("aria-label"))!.split(":")[0];
  await free.click();
  await a.getByRole("dialog").getByRole("button", { name: `Crear partida en ${loc}` }).click();

  await register(b, `b${id}`);
  await b.getByRole("button", { name: `${loc}: join` }).click();
  await b.getByRole("dialog").getByRole("button", { name: `Unirse a la partida en ${loc}` }).click();

  await placeFleet(a, fleetA, false);
  await placeFleet(b, fleetB, true);

  await a.getByRole("button", { name: "Emote 1" }).click();
  await expect(b.getByRole("img", { name: /^Emote 1 de / })).toBeVisible();

  for (let t = 0; t < 4; t++) {
    const mine = [...cellsOfB, "J1", "J2", "J3"].slice(t * 5, t * 5 + 5); // el salvo exige 5 celdas
    await fire(a, mine);
    await fire(b, water.slice(t * 5, t * 5 + 5));
  }

  await expect(a.getByRole("heading", { name: "Ganaste" })).toBeVisible();
  await expect(b.getByRole("heading", { name: "Perdiste" })).toBeVisible();
});
