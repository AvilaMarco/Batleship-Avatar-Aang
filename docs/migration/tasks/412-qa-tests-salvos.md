# 412 · qa · Tests de salvos: dominio y contrato v2.1 (CONDICIONAL, test primero)
Estado: hecha
Fase: F4
Depende de: 411
Paralelizable: no

Objetivo: fijar las reglas de la 411 con tests unitarios (verdes al terminar) y el contrato REST/STOMP de ADR 004 Anexo F con tests de integración (rojos hasta la 413).

Archivos (`T = src/test/java/dev/racnar/shipbender`): nuevo `T/unit/domain/SalvoRulesTest.java`, `T/integration/GameRestIntegrationTest.java`, `T/integration/GameEventsWebSocketTest.java`.

Casos (cerrados):
- Unitarios: disparar antes de `IN_GAME` → `GAME_NOT_IN_PROGRESS`; dos veces el mismo turno → `SALVO_ALREADY_FIRED`; 4 o 6 celdas, repetidas o ya disparadas → `INVALID_SALVO`; primer disparo del turno → `Optional.empty()`; segundo → `TurnResult` con hits/misses correctos y `turn` + 1; hundir toda la flota → `finished`, ganador 3 y perdedor 0; ambas flotas caen en el mismo turno → empate (1 y 1, `winnerId` null).
- REST: `POST /api/games/{id}/salvos` → 201 y `$.me.salvos`; errores con los códigos de arriba; el `GET` del rival no muestra los barcos aunque tenga los salvos.
- STOMP: `SALVO_FIRED` sin celdas; `TURN_RESOLVED` tras el segundo; `GAME_FINISHED` con `scores`; `GET /api/players/me` refleja los puntos.

Criterios de aceptación:
- `./mvnw test -Dtest=SalvoRulesTest` (sin Docker) → verde.
- `./mvnw test`: solo fallan los de integración nuevos; lista en el Resultado.
- Commit `test: salvos v2.1 (tarea 412, rojo hasta 413)`.

## Resultado

- `T/unit/domain/SalvoRulesTest.java`: 11 tests en VERDE, sin Spring ni Docker (ejecutado con `DOCKER_HOST=tcp://127.0.0.1:1`: BUILD OK). Cubren los 7 casos de la tarea mas: 4 y 6 celdas, repetidas y ya disparadas por separado, y "una flota hundida pero el rival no disparo: la partida sigue abierta" (decision de la 411); `sunk` por turno (no acumulado).
- `GameRestIntegrationTest` (+7) y `GameEventsWebSocketTest` (+3): contrato v2.1.
- `./mvnw test`: 99 tests, 89 verdes (los 78 previos + 11 unitarios), 10 rojos. Todos los rojos son "endpoint aun no existe" (`POST /api/games/{id}/salvos` -> 404, `Status expected:<201> but was:<404>`); ninguno es error del test:
  - REST: `fire_validSalvo_returnsCreatedWithOwnSalvoInView`, `fire_beforeGameStarts_is409GameNotInProgress`, `fire_twiceInSameTurn_is409SalvoAlreadyFired`, `fire_withFourCells_is400InvalidSalvo`, `fire_withCellOutsideGrid_is400InvalidSalvo`, `fire_asNonParticipant_is403NotAParticipant`, `getGame_rivalView_showsMySalvosButNeverMyShips`.
  - STOMP: `firstSalvo_rivalReceivesSalvoFiredWithoutCells`, `secondSalvo_bothReceiveTurnResolvedWithHitsMissesAndSunk`, `sinkingWholeFleet_bothReceiveGameFinishedWithScores_andProfileReflectsPoints`.
- Nota para 413: `fire_withCellOutsideGrid` exige 400 `INVALID_SALVO` (Anexo F), pero `Cell.parse` lanza `INVALID_FLEET`; la 413 debe traducir el codigo en el servicio/controlador. Los eventos GAME_FINISHED se esperan con `winner_id` y `scores` por id de jugador; el perfil se lee en `$.stats.score`.
