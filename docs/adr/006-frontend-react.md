# ADR 006 — Front nuevo: React + TS + Vite en `/frontend` (F5)

Estado: propuesta · 2026-11-21 · Implementa ADR 001 punto 3 contra el contrato congelado de ADR 004 (incluye salvos v2.1 de la tarea 413).

## Decisión
1. **Estructura** (Anexo A): `src/domain/` = reglas del juego en TS puro (sin React, sin `fetch`, sin STOMP), `src/api/` = cliente tipado, `src/screens/` + `src/components/` = React. Sin librería de estado ni de UI: `useState`/`useReducer` + un `AuthContext`; CSS plano (CSS Modules si hace falta).
2. **Cliente tipado a mano** (`api/types.ts`) copiando los esquemas de ADR 004 tal cual (snake_case, enums en mayúsculas). `api/http.ts`: un `request<T>()` que agrega `Authorization`, convierte `application/problem+json` en `ApiError {status, code, detail, errors}` y ante 401 borra el token y va a `#/login`. STOMP con `@stomp/stompjs` (WebSocket nativo, heartbeat 10000/10000, header `Authorization` en CONNECT).
3. **Sincronización**: la pantalla de partida hace `GET /api/games/{id}`, se suscribe a `/topic/games/{id}` y `/user/queue/errors`, y ante cualquier evento salvo `EMOTE` vuelve a pedir el `GET` (el server publica after-commit). Al reconectar, igual. `EMOTE` solo muestra la burbuja. La fase de la partida y el resultado final se derivan del `GameView` con funciones puras (`phase`, `outcome`; Anexo B).
4. **JWT**: se guarda el string que devuelve el login (`"Bearer eyJ..."`) en `sessionStorage` en desarrollo (`import.meta.env.DEV`: A contra B en dos pestañas) y en `localStorage` en el build de producción (la sesión sobrevive al cerrar la pestaña); decisión del usuario U2. Se elige en un solo lugar (`api/auth.ts`). Se manda igual en HTTP y STOMP; nunca en URL ni en logs. Logout = borrar la clave.
5. **Routing por hash** con un hook propio (`#/login`, `#/`, `#/games/{id}`): 3 rutas, sin dependencia y sin tocar el backend (no hace falta fallback SPA).
6. **Testing**: Vitest + jsdom + Testing Library. Los devs testean su código nuevo (dominio al 100 % de sus ramas, pantallas por comportamiento con `fetch` stubbeado). E2E: Playwright (solo Chromium), dos contextos de navegador juegan una partida entera (tarea 511).
7. **Dev**: Vite en **4705** `--strictPort`, proxy de `/api` y `/the-last-shipbender` (`ws: true`) al backend en **4704**, reescribiendo `Origin` al del backend (Anexo C). **Prod** (ADR 001: dentro del jar): perfil Maven `frontend` con `frontend-maven-plugin` que corre `npm ci && npm run build` y copia `frontend/dist` a `static/` del jar; `./mvnw test` sigue sin necesitar Node.
8. Se retira el front viejo (`src/main/resources/static`) y su webjar; los assets que se usan se copian a `frontend/public/assets`.

Decisiones del usuario (2026-11-21): U1 Vite 4705 + backend 4704 · U2 token `sessionStorage` en dev / `localStorage` en prod · U3 el usuario borra `static/` · U4 e2e con Playwright · U5 UI en español.

## Alternativas descartadas
- Redux/Zustand/TanStack Query: un solo recurso que se recarga entero por evento; el estado cabe en una pantalla. React Router: 3 rutas y obligaría a un fallback SPA en Spring Security.
- Tipos generados desde OpenAPI (springdoc): ~60 líneas de tipos, y el contrato ya está congelado; agrega un paso de build. Aplicar eventos al estado local (reducer de eventos): duplica en TS las reglas del backend (hits, turnos); el `GET` es la única fuente de verdad.
- Un solo almacenamiento para dev y prod: o no se prueba A contra B en pestañas (`localStorage`) o se pierde la sesión al cerrar (`sessionStorage`). Cookie `httpOnly`: requiere cambiar auth en backend y agregar CSRF.
- Servir el front aparte (Nginx/Cloudflare Pages): CORS y dos despliegues (ya descartado en ADR 001).

## Consecuencias
- Un solo servidor de desarrollo de F5 a la vez (usa los dos puertos de prueba). En dev el token vive por pestaña; en prod persiste.
- El token en web storage sigue expuesto a XSS: prohibido `dangerouslySetInnerHTML` (criterio de review, tarea 512).
- Lo que el contrato no da se deriva en el cliente: ganador al recargar una partida `FINISH` (de `sunk`), y qué partidas son mías (de `GameSummary.players` + `GET /api/players/me`).

---

## Anexo A · Estructura
```
frontend/
  package.json  vite.config.ts  tsconfig*.json  index.html  playwright.config.ts (511)
  public/assets/{emotes,img,ships,icons}/      copiados de src/main/resources/static/web/assets
  e2e/full-game.spec.ts                        (511)
  src/
    main.tsx  App.tsx (rutas por hash)  router.ts (useHashRoute)
    domain/   cell.ts  fleet.ts  salvo.ts  board.ts  phase.ts  map-locations.ts   (TS puro + *.test.ts)
    api/      types.ts  http.ts  auth.ts  players.ts  games.ts  stomp.ts           (+ *.test.ts)
    auth/     AuthContext.tsx
    screens/  LoginScreen.tsx  LobbyScreen.tsx  GameScreen.tsx                    (+ *.test.tsx)
    components/ Board.tsx  ShipPicker.tsx  NationPicker.tsx  GameMap.tsx  Ranking.tsx  EmoteBar.tsx
```
Regla verificable: `grep -rlE "from ['\"]react|@stomp|fetch\(|sessionStorage" frontend/src/domain` → sin salida.

## Anexo B · API del dominio TS (firmas cerradas)
```ts
export type CellId = string;                                   // "A1".."J10"
export const ROWS = "ABCDEFGHIJ", COLS = 10;
export function isCell(s: string): boolean;                    // /^[A-J]([1-9]|10)$/
export type Orientation = "H" | "V";
export const SHIP_SIZES: Record<ShipType, number>;             // CARRIER 5, BATTLESHIP 4, SUBMARINE 3, DESTROYER 3, PATROL_BOAT 2
export function shipCells(type: ShipType, origin: CellId, o: Orientation): CellId[] | null; // null si se sale de la grilla
export function canPlace(placed: ShipPlacement[], ship: ShipPlacement): boolean;            // sin solape, tipo no repetido
export function isFleetComplete(placed: ShipPlacement[]): boolean;                          // los 5 tipos
export function toggleTarget(selected: CellId[], cell: CellId, alreadyFired: CellId[]): CellId[]; // máx 5, sin repetir ni ya disparadas
export const SALVO_SIZE = 5;
export type CellState = "empty" | "ship" | "hit" | "miss" | "pending";
export function myBoard(view: GameView): Record<CellId, CellState>;      // mis barcos + disparos resueltos del rival
export function enemyBoard(view: GameView): Record<CellId, CellState>;   // mis disparos: hit/miss resueltos, pending si el rival no disparó ese turno
export type Phase = "WAITING_OPPONENT" | "PLACE_SHIPS" | "WAITING_OPPONENT_SHIPS" | "FIRE" | "WAITING_OPPONENT_SALVO" | "FINISHED";
export function phase(view: GameView): Phase;   // FINISH→FINISHED; opponent null→WAITING_OPPONENT; !me.ships_placed→PLACE_SHIPS;
                                                // !opponent.ships_placed→WAITING_OPPONENT_SHIPS; me.salvos tiene turn==view.turn→WAITING_OPPONENT_SALVO; si no FIRE
export function outcome(view: GameView): "WON" | "LOST" | "TIED" | null; // null si no FINISH; 5 hundidos de cada lado = TIED
```

## Anexo C · `vite.config.ts` (lo no obvio)
```ts
const backend = process.env.BACKEND_URL ?? "http://localhost:4704";
const rewriteOrigin = (p: any) => {                 // Spring rechaza el handshake WS si Origin ≠ host (WebSocketConfig sin allowedOrigins)
  p.on("proxyReq", (r: any) => r.setHeader("origin", backend));
  p.on("proxyReqWs", (r: any) => r.setHeader("origin", backend));
};
server: { port: 4705, strictPort: true, proxy: {
  "/api": { target: backend, changeOrigin: true, configure: rewriteOrigin },
  "/the-last-shipbender": { target: backend, ws: true, changeOrigin: true, configure: rewriteOrigin } } }
```
URL del WebSocket en el cliente: `${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/the-last-shipbender` (igual en dev y prod).
