# 506 · frontend-dev · Pantalla de partida (1/2): sincronización y colocación de barcos
Estado: parcial
Fase: F5
Depende de: 505
Paralelizable: no
Origen: inventario de `src/main/resources/static/web/game.html` (grilla, rotar barco, "Send Ships"); ADR 006 punto 3; ADR 004 A.2 y B.
Riesgo: normal

Objetivo: entrar a `#/games/{id}`, esperar rival, colocar los 5 barcos y ver cuándo arranca la partida.

Archivos: `frontend/src/screens/GameScreen.tsx` + `GameScreen.test.tsx`, `frontend/src/components/{Board,ShipPicker}.tsx`.

Diseño (cerrado):
- `GameScreen`: `GET /api/games/{id}` → `view`; `connectGame` (503) con `onConnect` y `onEvent` (todo evento salvo `EMOTE`) = volver a pedir el `GET`; `disconnect` al desmontar. Error 403/404 → mensaje y link al lobby.
- Render según `phase(view)` (502): `WAITING_OPPONENT` / `WAITING_OPPONENT_SHIPS` → mensaje; `PLACE_SHIPS` → `ShipPicker` + `Board`; `FIRE`/`WAITING_OPPONENT_SALVO`/`FINISHED` → placeholder hasta 507 (solo `myBoard`).
- `Board`: 10×10 de `<button aria-label="B7">` con clase por `CellState`; encabezados A-J / 1-10; recibe `cells`, `onCellClick`, `disabled`. Reutilizable para el tablero rival en 507.
- Colocación por clic (sirve en móvil, sin drag): elegir tipo en `ShipPicker` (imagen `assets/ships/<tipo>.png`, tamaño), botón "Rotar" (H/V), clic en la celda origen → `shipCells` + `canPlace`; clic en un barco colocado lo quita. "Enviar barcos" habilitado con `isFleetComplete` → `POST /api/games/{id}/ships` → `view` de la respuesta. `INVALID_FLEET` muestra `detail`.

Criterios de aceptación (tests primero; `api/games` y `api/stomp` mockeados con `vi.mock`):
- Test: con `view` en `PLACE_SHIPS`, colocar los 5 barcos por clics habilita "Enviar barcos" y llama `placeShips` con 5 `ShipPlacement`; un evento `GAME_STARTED` provoca un segundo `getGame`; un evento `EMOTE` no.
- `cd frontend && npm test && npm run build` → exit 0.
- Manual en 4705 (backend 4704), dos pestañas: ambos colocan y ambos pasan a la fase de disparo sin recargar. Si no hay Docker: `Estado: parcial`.
- Commit `feat(front): partida, sincronización y colocación de barcos (tarea 506)`.

## Resultado

- `GameScreen` (+ `Board`, `ShipPicker`) cableada en `App.tsx` para `#/games/{id}`: GET, STOMP (todo evento salvo EMOTE recarga), disconnect al desmontar, 403/404 con link al lobby, colocación por clic con rotar/quitar, envío con `INVALID_FLEET` en `role="alert"`. FIRE/WAITING_OPPONENT_SALVO/FINISHED: solo `myBoard` (507).
- TDD: fase roja ejecutada de verdad (suite falló por módulo `GameScreen` inexistente), luego verde. 6 tests nuevos.
- `cd frontend && npm test && npm run build`: 61 tests OK, build OK (exit 0).
- Sin estilos CSS para clases `ship/hit/miss/pending` (celdas sin color aún): pendiente para 507/estilos.
- Manual en 4705 con dos pestañas: **pendiente de verificar** (no se levantó backend/Vite). Por eso Estado parcial.
