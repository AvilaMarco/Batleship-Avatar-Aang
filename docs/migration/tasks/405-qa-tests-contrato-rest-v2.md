# 405 · qa · Tests de integración del contrato REST v2 (test primero)
Estado: pendiente
Fase: F4
Depende de: 403 (y ADR 004 aprobada)
Paralelizable: sí, con 404 (archivos distintos)

Objetivo: reemplazar los tests de caracterización del contrato v1 por tests del contrato REST v2 (ADR 004 Anexos A, C y E). **Se espera que queden en rojo** hasta 406 (auth/jugadores) y 407 (partidas): son la especificación ejecutable para backend-dev (ADR 005 punto 10). Los "bug conocido" pasan a afirmar el comportamiento correcto.

Archivos (`T = src/test/java/dev/racnar/shipbender/integration`): `T/support/AuthHelper.java`, `T/AuthIntegrationTest.java`, `T/MatchFlowIntegrationTest.java` (renombrar a `GameRestIntegrationTest.java`), `T/SecurityRulesIntegrationTest.java`, `T/GameplayWebSocketTest.java` (solo lo indicado), borrar `T/MatchControllerIntegrationTest.java` (lo cubre `GameRestIntegrationTest`).

Casos (cerrados):
- `AuthHelper`: login en `POST /api/auth/login`; agregar `createGame(mvc, token)` → id (`POST /api/games` con location única) y `join(mvc, token, id)` para reutilizar en 408.
- `AuthIntegrationTest`: login OK (`email`, `token` con `Bearer `); contraseña mala → 401 `code=INVALID_CREDENTIALS`; email mal formado → 400 `code=VALIDATION_FAILED` y `$.errors.email`; registro → 201 y repetido → 409 `EMAIL_IN_USE`; `PATCH /api/players/me {"nation":"FIRE"}` → 200 `$.nation=FIRE`; nación `"PLUTO"` → 400 `MALFORMED_REQUEST`; `GET /api/players/me` → 200 con `email` y `stats.win_rate`; sin header → 401 `UNAUTHENTICATED`; firma manipulada → **401 `UNAUTHENTICATED`** (antes: `assertThrows`); `GET /api/players/ranking` → 200, array, ninguna entrada con `email`, sin jugadores repetidos.
- `GameRestIntegrationTest`: `GET /api/games` sin token → 401; con token → array de `GameSummary` sin `email` en `players`; crear → 201, header `Location`, `$.status=CREATED`, `$.opponent` null, `$.me.ships_placed=false`; misma location → 409 `LOCATION_TAKEN`; unirse → 201 y `$.opponent.player.id` presente; tercero → 409 `GAME_FULL`; **unirse a la propia → 409 `ALREADY_IN_GAME`** (antes: aceptado); `GET` de un ajeno → **403 `NOT_A_PARTICIPANT`** (antes: 409 "Player Already in Game"); id inexistente → 404 `GAME_NOT_FOUND`.
- Barcos (en `GameRestIntegrationTest`, flota clásica válida `A1-A5, B1-B4, C1-C3, D1-D3, E1-E2`): A coloca → 201, `$.me.ships_placed=true`, `$.me.ships` con 5; **segundo envío → 409 `SHIPS_ALREADY_PLACED`**; **flota inválida (`Z99`, tamaño mal, solape) → 400 `INVALID_FLEET`** y `GET` sigue con `ships_placed=false` (antes: aceptada); ajeno → 403; B coloca después → `status=IN_GAME` y **el `GET` de A sigue mostrando sus 5 barcos** (verifica la sospecha de `GameService.java:39-46`, ADR 005 Anexo C); en el `GET` de A, `$.opponent` no tiene `ships`.
- `SecurityRulesIntegrationTest`: rutas protegidas v2 sin token (`GET /api/games`, `GET /api/games/1`, `PATCH /api/players/me`) → 401; no listadas con token (`/api/inventado`, `/h2-console/`) → 403; públicas igual que hoy.
- `GameplayWebSocketTest`: borrar `sendShips_validShips_...` y `sendShips_invalidShips_..._knownBug` (contrato v1 que desaparece; v2 en 405 por REST y en 408 por eventos) y el helper `hostStatus`; `createAndJoin` usa `AuthHelper`. `connectWithoutToken_...` y `..._heartbeat` no cambian.

Criterios de aceptación:
- `grep -rn "knownBug\|bug conocido\|/api/match\|/api/players/nation\|/api/players/login" src/test` → sin salida.
- `./mvnw test`: compila; `CellTest/FleetTest/GameTest/PlayerStatsTest` (si la 404 ya está) y los 2 de STOMP de `GameplayWebSocketTest` en verde; los nuevos de REST en rojo. Anotar en el Resultado la lista exacta de los que fallan (es el criterio de 406 y 407).
- Commit `test: contrato REST v2 (tarea 405, rojo hasta 407)`.

## Resultado
