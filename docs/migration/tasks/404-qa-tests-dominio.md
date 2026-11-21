# 404 · qa · Tests unitarios del dominio (sin Spring) y baja de los tests de servicios
Estado: borrador
Fase: F4
Depende de: 403
Paralelizable: sí, con 405 y con 406 (archivos distintos; sesiones en worktrees separados si corren a la vez)

Objetivo: fijar las reglas del juego con tests rápidos, sin Spring ni BD, y borrar los tests unitarios que mockean servicios (`MatchServiceTest`, `GameValidationTest`): sus reglas pasan a `Game` y, si siguieran, impedirían a backend-dev cambiar las firmas de los servicios en 406-407.

Archivos (`T = src/test/java/dev/racnar/shipbender`): nuevos `T/unit/domain/CellTest.java`, `FleetTest.java`, `GameTest.java`, `PlayerStatsTest.java`; borrar `T/unit/service/MatchServiceTest.java` y `T/unit/service/GameValidationTest.java`.

Casos (cerrados; afirmar siempre `ShipbenderException.code()`, no el texto):
- `CellTest`: `A1` y `J10` válidas y `toString` ida y vuelta; `K1`, `A0`, `A11`, `a1`, `1A`, `""` → `INVALID_FLEET`.
- `FleetTest`: flota clásica válida (horizontal y vertical, celdas desordenadas); falta un tipo; tipo repetido; tamaño incorrecto; celdas no consecutivas (`A1,A3`); diagonal; solape entre dos barcos → todos `INVALID_FLEET`.
- `GameTest`: con 1 jugador `status()` = `CREATED` y `opponentOf` vacío; `join` de un segundo jugador lo agrega; tercero → `GAME_FULL`; el mismo jugador dos veces → `ALREADY_IN_GAME`; `participant` de un ajeno → `NOT_A_PARTICIPANT`; `placeShips` de un ajeno → `NOT_A_PARTICIPANT`; dos veces → `SHIPS_ALREADY_PLACED`; los dos colocaron → `IN_GAME`; con `finishDate` → `FINISH`; ids > 127 (reemplaza `containsPlayer_withIdsAbove127`).
- `PlayerStatsTest`: `[3, 0]` → won 1, lost 1, `winRate` 50.0 (en v1 daba 0 por división entera, `PlayerService.java:104`); `[]` → todo 0; `[1]` → tied 1, score 1.
- Los ids se asignan con los setters de Lombok (`player.setId(...)`).

Criterios de aceptación:
- `grep -rln "springframework\|Mockito" src/test/java/dev/racnar/shipbender/unit` → sin salida.
- Con Docker detenido: `./mvnw test -Dtest='CellTest,FleetTest,GameTest,PlayerStatsTest'` → BUILD SUCCESS, 0 fallos.
- `./mvnw test` (con Docker) → 0 fallos (si la 405 ya está commiteada, solo fallan los tests v2 de la 405).
- Commit `test: reglas del dominio sin Spring (tarea 404)`.

## Resultado
