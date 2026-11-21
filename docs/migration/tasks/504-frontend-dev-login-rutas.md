# 504 · frontend-dev · Login, registro, sesión y rutas por hash
Estado: hecha
Fase: F5
Depende de: 503
Paralelizable: no
Origen: inventario del front viejo (`src/main/resources/static/index.html`: login + registro); ADR 006 puntos 4 y 5.
Riesgo: alto (sesión/JWT) → revisión en 512

Objetivo: entrar al juego con un usuario nuevo o existente y salir.

Archivos: `frontend/src/{App.tsx,router.ts}`, `frontend/src/auth/AuthContext.tsx`, `frontend/src/screens/LoginScreen.tsx` + `LoginScreen.test.tsx`, `router.test.ts`.

Diseño (cerrado):
- `router.ts`: `useHashRoute()` → `{name: "login"} | {name: "lobby"} | {name: "game", id: number}` a partir de `#/login`, `#/`, `#/games/{id}`; escucha `hashchange`. `navigate(hash)`.
- `AuthContext`: `profile: PlayerProfile | null`, `login`, `register` (= `POST /api/players` y luego login), `logout` (= `clearToken()` y `#/login`), `refreshProfile`. Al montar: si hay token → `GET /api/players/me`; si no hay token → `#/login`.
- `App.tsx`: ruta `login` sin sesión; `lobby`/`game` exigen `profile` (si no, `#/login`). `lobby` y `game` pueden ser placeholders hasta 505/506.
- `LoginScreen`: un formulario con modo Entrar/Registrarse (nombre solo al registrarse), `<label>` en cada campo, errores del `ApiError`: `INVALID_CREDENTIALS`, `EMAIL_IN_USE` y `errors` por campo de `VALIDATION_FAILED`. Texto como React (nunca `dangerouslySetInnerHTML`).

Criterios de aceptación (tests primero):
- `LoginScreen.test.tsx`: login OK navega a `#/`; 401 muestra "credenciales" (mensaje visible); registro con 409 muestra el error de email; `router.test.ts` parsea las 3 rutas y una inválida → `lobby`.
- `cd frontend && npm test && npm run build` → exit 0.
- `grep -rn "dangerouslySetInnerHTML" frontend/src` → sin salida.
- Manual (backend 4704 + `npm run dev`): registrarse, recargar la página (sigue logueado), salir (vuelve a login). Si no hay Docker: `Estado: parcial`.
- Commit `feat(front): login, registro y rutas (tarea 504)`.

## Resultado
Implementado: `router.ts` (parseRoute/navigate/useHashRoute), `auth/AuthContext.tsx` (AuthProvider, useAuth, `ready` mientras se valida el token), `screens/LoginScreen.tsx` (errores en `role="alert"`, texto React), `App.tsx` (guardas de ruta; lobby/game placeholders).
Verificado: `npm test` 49/49 exit 0, `npm run build` exit 0, grep `dangerouslySetInnerHTML` sin salida. Tests con `fireEvent` (no se añadió user-event).
Pendiente de verificar: prueba manual con backend (registrarse, recargar, salir), la hace el chat central.
Nota: el token solo se toca en `api/auth.ts`; `AuthContext` usa `getToken/clearToken` de ahí.

Verificación manual cerrada por el chat central (navegador, backend 4704 + Vite 4705 y después el front servido por el jar): ver Resultado F5 en PLAN.md. E2E de Playwright (tarea 511) cubre el flujo completo.
