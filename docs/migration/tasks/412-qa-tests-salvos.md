# 412 · qa · Tests de salvos: dominio y contrato v2.1 (CONDICIONAL, test primero)
Estado: borrador · condicional: solo si el usuario aprueba D2
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
