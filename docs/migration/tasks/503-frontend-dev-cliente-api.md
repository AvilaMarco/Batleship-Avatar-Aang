# 503 · frontend-dev · Cliente REST y STOMP tipado + manejo del token
Estado: parcial
Fase: F5
Depende de: 501
Paralelizable: sí, con 502 y con 508-510
Origen: ADR 006 puntos 2, 3 y 4; contrato ADR 004 (Anexos A, B, C, F).
Riesgo: alto (manejo del JWT) → revisión en 512

Objetivo: toda la comunicación con el backend en `src/api/`, tipada, sin pantallas todavía.

Archivos: `frontend/src/api/{types,http,auth,players,games,stomp}.ts` + `http.test.ts`, `stomp.test.ts`; dependencia `@stomp/stompjs`.

Diseño (cerrado):
- `types.ts`: todos los esquemas de ADR 004 A.3 y F (`PlayerProfile`, `PlayerSummary`, `RankingEntry`, `GameSummary`, `GameView` con `salvos`/`sunk`, `ShipPlacement`, `GameEvent` como unión discriminada por `type` con los 7 eventos de `GameEvent.java`, `ProblemDetail`). snake_case tal cual.
- `auth.ts`: `getToken()`, `setToken(t)`, `clearToken()` sobre `const store = import.meta.env.DEV ? sessionStorage : localStorage`, clave `shipbender.token`. Se guarda el `token` del login tal cual (`"Bearer eyJ..."`). Único archivo que toca web storage. Test: con `DEV` verdadero escribe en `sessionStorage`, con falso en `localStorage` (`vi.stubEnv`).
- `http.ts`: `request<T>(method, path, body?)`; agrega `Authorization` si hay token y `Content-Type: application/json` si hay body; respuesta no-OK → `throw new ApiError(problem)` (`status`, `code`, `detail`, `errors?`); 401 en ruta distinta de `/api/auth/login` → `clearToken()` y `location.hash = "#/login"`. Nunca loguea headers ni token.
- `players.ts`/`games.ts`: una función por fila de ADR 004 A.1, A.2 y `fireSalvo(id, cells)` (Anexo F). Login: `login(email, password)` guarda el token.
- `stomp.ts`: `connectGame(gameId, {onEvent, onError, onConnect})` → `{sendEmote(emote), disconnect()}`. `@stomp/stompjs` `Client` con `brokerURL` de ADR 006 Anexo C, `connectHeaders: {Authorization: getToken()}`, heartbeats 10000/10000, `reconnectDelay: 5000`; en cada `onConnect` se suscribe a `/topic/games/{id}` y `/user/queue/errors` y llama `onConnect` (la pantalla hace el `GET`). `sendEmote` publica JSON `{emote}` con `content-type: application/json` a `/app/games/{id}/emotes`.

Criterios de aceptación (tests primero, `fetch` con `vi.stubGlobal`):
- `http.test.ts`: manda `Authorization` con el token guardado; 409 problem+json → `ApiError` con `code`; 401 → token borrado y hash `#/login`; 401 del login → no redirige.
- `stomp.test.ts`: con `Client` mockeado (`vi.mock("@stomp/stompjs")`), `connectHeaders.Authorization` = token y las 2 suscripciones al conectar.
- `cd frontend && npm test && npm run build` → exit 0.
- `grep -rn "sessionStorage\|localStorage" frontend/src --include=*.ts --include=*.tsx | grep -v test | grep -v "src/api/auth.ts"` → sin salida.
- Commit `feat(front): cliente REST y STOMP tipado según ADR 004 (tarea 503)`.

## Resultado
Estado final: parcial (todo hecho salvo la verificación del WebSocket real).

Hecho: `frontend/src/api/{types,auth,http,players,games,stomp}.ts` + `auth.test.ts`, `http.test.ts`, `stomp.test.ts`; dependencia `@stomp/stompjs`. Tests escritos primero (rojo: módulos inexistentes) y luego verdes.
Verificado: `npm test` exit 0 (9 archivos, 43 tests); `npm run build` exit 0; grep de web storage fuera de `src/api/auth.ts` sin salida; sin `console.*` en `src/api`; el token solo viaja en header `Authorization`.
Decisiones: `ApiError` con `code` "UNKNOWN" si el error no es problem+json; `brokerURL` se deriva de `location` (ws/wss) y depende del proxy de Vite en dev; `fireSalvo` apunta al Anexo F (backend aún no lo implementa).
NO VERIFICADO: conexión STOMP real contra el backend (handshake, headers, heartbeats, suscripciones); solo probado con `Client` mockeado. Pendiente en la tarea e2e/512.
