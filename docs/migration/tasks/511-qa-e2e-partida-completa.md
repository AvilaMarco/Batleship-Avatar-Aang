# 511 · qa · E2E: partida completa entre dos jugadores (Playwright)
Estado: hecha
Fase: F5
Depende de: 507, 510
Paralelizable: no (usa los puertos 4704 y 4705)
Origen: PLAN.md criterio de cierre F5 "Partida completa jugable en React + TS"; ADR 006 punto 6.
Riesgo: normal

Objetivo: un test automatizado que demuestre el criterio de cierre de F5 de punta a punta.

Archivos (permitidos a qa en esta tarea): `frontend/e2e/full-game.spec.ts`, `frontend/playwright.config.ts`, `frontend/package.json` (solo devDependency `@playwright/test` y script `"e2e": "playwright test"`), `.gitignore` (`frontend/test-results/`, `frontend/playwright-report/`). Excluir `e2e/` de Vitest si lo levanta.

Diseño (cerrado):
- `playwright.config.ts`: solo Chromium, `baseURL: "http://localhost:4705"`, `webServer: {command: "npm run dev", port: 4705, reuseExistingServer: true}`. El backend lo levanta quien corre el test (`PORT=4704 ./mvnw spring-boot:test-run`, requiere Docker).
- Un spec, dos `browser.newContext()` (A y B, emails únicos con `Date.now()`): registro → A crea en un punto libre del mapa → B se une → ambos colocan una flota fija por clics (A en filas A-E horizontal, B en columnas 1-5 vertical, o similar conocida) → ambos disparan por turnos; A apunta a las 17 celdas de B, B a celdas de agua → en ≤4 turnos A ve "Ganaste" y B "Perdiste". En el camino: A manda un emote y B lo ve.
- Selectores por rol/label (`getByRole("button", {name: "B7"})`), sin `waitForTimeout`.

Criterios de aceptación:
- Con backend en 4704: `cd frontend && npx playwright install chromium && npm run e2e` → 1 test verde, 2 corridas seguidas.
- `cd frontend && npm test` → sigue verde (Vitest no ejecuta `e2e/`).
- Si un fallo es del producto: no lo arregles; crea `5NN-frontend-dev-<bug>.md` (o backend-dev) y deja el test marcado `test.fixme` con el motivo.
- Si no hay Docker o no se pueden bajar navegadores: `Estado: parcial` (lo verifica el central a mano con el mismo guion).
- Commit `test: e2e de partida completa con Playwright (tarea 511)`.

## Resultado
Creado `frontend/e2e/full-game.spec.ts` + `playwright.config.ts`, script `e2e`, devDependency `@playwright/test`, exclusión de `e2e/` en Vitest (`vite.config.ts`) e ignorados en `.gitignore`.
Flujo: registro + nación Agua (obligatoria antes del mapa) → A crea → B se une → flotas fijas → emote visible para B → 4 turnos (el salvo exige 5 celdas: el último lleva 2 impactos + 3 agua) → "Ganaste"/"Perdiste".
Verificado: `npm run e2e` verde 2 veces seguidas con backend en 4704; `npm test` verde. Sin bugs de producto; los tableros se distinguen por su aria-label de grupo ("Tablero rival").
