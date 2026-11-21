# 411 · backend-dev · Dominio de salvos, turnos y fin de partida (CONDICIONAL)
Estado: pendiente
Fase: F4
Depende de: 410
Paralelizable: no

Objetivo: implementar en el dominio las reglas de ADR 004 Anexo F. Hoy no existen (`Salvo.goodShoot`/`shipsDead` nunca se llaman; `Game.turn` nunca cambia). Sin endpoint todavía (lo hace la 413).

Archivos (`P = src/main/java/dev/racnar/shipbender`): `P/game/domain/{Game,GamePlayer,Salvo}.java`, nuevos `P/game/domain/TurnResult.java`, `P/shared/error/ErrorCode.java` (agregar `GAME_NOT_IN_PROGRESS`, `SALVO_ALREADY_FIRED`, `INVALID_SALVO`).

Diseño (cerrado; 5 disparos fijos por turno salvo que el usuario elija otra regla al resolver D2 en ADR 004):
- `Game.fire(long playerId, List<Cell> cells)` → `Optional<TurnResult>`: guardas en orden participante → `status() == IN_GAME` → no disparó en `turn` → exactamente 5 celdas, sin repetir y no disparadas antes por ese jugador. Guarda un `Salvo(turn, locations)`. Si ambos dispararon el turno: calcula impactos y hundidos de cada uno, avanza `turn` o termina (`finishDate`, `score` 3/1/0, empate si ambas flotas caen en el mismo turno) y devuelve el `TurnResult`; si falta el rival, `Optional.empty()`.
- `TurnResult(int turn, List<PlayerTurn> results, boolean finished, Long winnerId)`, `PlayerTurn(long playerId, List<Cell> hits, List<Cell> misses, List<ShipType> sunk)`.
- Impactos y hundidos se calculan desde `Ship.locations` y los `Salvo` del rival; no se persisten (se borró `salvosImpacted` en la 410).
- Sin Spring en `game/domain` (mismo grep que la 403).

Criterios de aceptación:
- `grep -rlE "org\.springframework|tools\.jackson|com\.fasterxml" src/main/java/dev/racnar/shipbender/game/domain` → sin salida.
- `./mvnw test` → 0 fallos (no cambia ningún endpoint).
- Commit `feat: salvos, turnos y fin de partida en el dominio (tarea 411)`.

## Resultado
