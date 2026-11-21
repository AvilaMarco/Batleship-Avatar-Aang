# 502 · frontend-dev · Dominio del juego en TS puro
Estado: hecha
Fase: F5
Depende de: 501
Paralelizable: sí, con 503 y con 508-510 (archivos distintos; commits separados)
Origen: ADR 006 punto 1 y Anexo B; agente frontend-dev "Lógica del juego en funciones puras TS".
Riesgo: normal

Objetivo: implementar las funciones del Anexo B de ADR 006 con tests, sin React.

Archivos: `frontend/src/domain/{cell,fleet,salvo,board,phase,map-locations}.ts` y un `*.test.ts` por archivo (salvo `map-locations`). `GameView`, `ShipPlacement`, `ShipType` se importan de `frontend/src/api/types.ts`: si 503 aún no lo creó, créalo tú solo con esos tipos copiados de ADR 004 A.3 (+ `salvos`/`sunk` de Anexo F) y 503 lo completa.

Diseño (cerrado):
- Firmas exactas del Anexo B. `fleet.ts` replica A.4 de ADR 004 solo para guiar al usuario; la validación real es la del backend.
- `map-locations.ts`: los 21 puntos de `src/main/resources/static/web/js/games-mjs/datosmap.js` como datos tipados `{id, nation: Nation, x, y, r}` (los datos, no el código).
- `board.ts`: los `hits` de un salvo vienen vacíos hasta que el turno se resuelve (`GameView.java`, método `salvos`); celda de un salvo del turno actual sin resolver = `pending`, resuelta y no en `hits` = `miss`.
- `outcome`: `FINISH` + `opponent.sunk.length === 5` y `me.sunk.length < 5` → `WON`; al revés `LOST`; ambos 5 → `TIED`.

Criterios de aceptación (tests primero):
- Tests cubren como mínimo: `shipCells` borde derecho/inferior → `null`; `canPlace` con solape y con tipo repetido; `toggleTarget` 6.ª celda ignorada, quitar una seleccionada, celda ya disparada ignorada; `phase` cada uno de los 6 valores; `outcome` los 3 resultados y `null`; `enemyBoard` con `pending`, `hit` y `miss`.
- `cd frontend && npm test && npm run build` → exit 0.
- `grep -rlE "from ['\"]react|@stomp|fetch\(|sessionStorage" frontend/src/domain` → sin salida.
- Commit `feat(front): dominio del juego en TS puro (tarea 502)`.

## Resultado

- `frontend/src/domain/{cell,fleet,salvo,board,phase,map-locations}.ts` + tests (+ `fixtures.ts` de apoyo a los tests). Usa los tipos de `api/types.ts` creado por 503.
- Decisiones: salvo sin resolver = `s.turn === view.turn` y status != FINISH (el turno avanza al resolverse); en FINISH todo salvo cuenta resuelto. `myBoard` no pinta los salvos sin resolver del rival. `outcome` devuelve `null` en FINISH si ninguna flota está hundida (seed). `phase`: FINISH tiene prioridad sobre `opponent null`.
- `npm test` exit 0 (43 tests); grep de pureza sin salida. `npm run build` falla solo por `src/api/http.test.ts:45-46` (TS18046, de 503), no por el dominio.
