# 413 · backend-dev · Endpoint de salvos y eventos de turno (CONDICIONAL)
Estado: pendiente
Fase: F4
Depende de: 412
Paralelizable: no

Objetivo: exponer `Game.fire` según ADR 004 Anexo F hasta `./mvnw test` todo en verde.

Archivos (`P = src/main/java/dev/racnar/shipbender`): `P/game/GameController.java`, `P/game/GameService.java`, `P/game/dto/*` (`SalvoRequest`, `GameView` con `salvos` y `sunk`), `P/game/event/GameEvent.java`.

Diseño (cerrado):
- `POST /api/games/{id}/salvos` `{cells}` → `GameService.fire(email, id, cells)` (`@Transactional`) → 201 `GameView`.
- Eventos: siempre `SalvoFired(gameId, playerId, turn)`; con `TurnResult`: `TurnResolved(gameId, turn, results)` y, si terminó, `GameFinished(gameId, winnerId, scores)`. Tipos `SALVO_FIRED`, `TURN_RESOLVED`, `GAME_FINISHED`. Mismo publicador after-commit de la 409.
- `GameView`: `me.salvos` y `opponent.salvos` (`turn`, `cells`, `hits`) y `sunk`; `opponent` sigue sin `ships`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 0 fallos.
- `grep -rn "convertAndSend" src/main/java` → solo en `GameEventStompPublisher`.
- Commit `feat: salvos v2.1 por REST con eventos de turno (tarea 413)`.

## Resultado
