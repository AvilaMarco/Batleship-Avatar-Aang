# 505 · frontend-dev · Lobby: nación, mapa de partidas, crear/unirse, ranking
Estado: parcial
Fase: F5
Depende de: 502, 504
Paralelizable: no
Origen: inventario de `src/main/resources/static/web/games.html` (elegir nación, mapa con ubicaciones, crear/unirse, ranking, logout); ADR 004 A.1-A.2.
Riesgo: normal

Objetivo: desde el lobby, crear una partida o unirse a una y llegar a `#/games/{id}`.

Archivos: `frontend/src/screens/LobbyScreen.tsx` + `LobbyScreen.test.tsx`, `frontend/src/components/{NationPicker,GameMap,Ranking}.tsx`.

Diseño (cerrado):
- Si `profile.nation === null`: `NationPicker` (4 cartas `public/assets/img/card-*.png`, botones) → `PATCH /api/players/me` → `refreshProfile`.
- `GameMap`: imagen `assets/img/fondo-mapa.png` con un botón por punto de `domain/map-locations.ts` (posicionado en % sobre el tamaño natural de la imagen, para móvil). Punto con partida de `GET /api/games` (match por `location`): muestra jugadores; "Unirse" si no estoy en ella y tiene 1 jugador; "Volver" si estoy (por `players[].id === profile.id`). Punto libre: "Crear" → `POST /api/games {nation: punto.nation, location: punto.id}`.
- Crear/unirse/volver → `navigate("#/games/{id}")`. Errores `LOCATION_TAKEN`, `GAME_FULL`, `ALREADY_IN_GAME` se muestran y se recarga la lista.
- `Ranking`: tabla con `player.name`, `score`, `won`, `tied`, `lost` de `GET /api/players/ranking` (sin email: no viene en el contrato).
- Botón "Salir" → `logout`. Sin polling: botón "Actualizar" para la lista.
- Si el diff pasa de ~400 líneas, `Ranking` va en una tarea aparte (crear `505b`).

Criterios de aceptación (tests primero, `fetch` stubbeado):
- Test: perfil sin nación muestra el selector y al elegir llama `PATCH`; con una partida ajena de 1 jugador el punto ofrece "Unirse" y al pulsarlo navega a `#/games/7`; con una partida propia ofrece "Volver".
- `cd frontend && npm test && npm run build` → exit 0.
- Manual en 4705 (backend 4704): usuario A crea en un punto; usuario B en otra pestaña (sessionStorage) lo ve y se une. Si no hay Docker: `Estado: parcial`.
- Commit `feat(front): lobby con mapa, partidas y ranking (tarea 505)`.

## Resultado
Hecho: `LobbyScreen`, `NationPicker`, `GameMap` (puntos en % sobre 1395x950), `Ranking`; `App.tsx` renderiza el lobby en `#/`. Tests (6 nuevos, `useAuth` mockeado y `fetch` stubbeado): selector->PATCH, Unirse->`#/games/7`, Volver, Crear (POST con nation/location), LOCATION_TAKEN con alert y recarga, ranking y Salir. `npm test` (55 pasan) y `npm run build` -> exit 0. Diff ~330 líneas: no hace falta 505b.
Notas: partida con 2 jugadores ajenos queda como botón deshabilitado "Partida llena". El botón "Salir" ahora vive solo en el lobby (el placeholder de partida no lo tiene hasta 506).
Pendiente de verificar: prueba manual en el navegador con backend (A crea, B se une).
