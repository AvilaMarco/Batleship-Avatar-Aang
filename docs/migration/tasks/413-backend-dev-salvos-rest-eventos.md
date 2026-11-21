# 413 · backend-dev · Endpoint de salvos y eventos de turno (CONDICIONAL)
Estado: hecha
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
`POST /api/games/{id}/salvos` -> `GameService.fire` (traduce el codigo de `Cell.parse` a `INVALID_SALVO`; `Cell.parse` y la 403 intactos). Eventos `SALVO_FIRED`, `TURN_RESOLVED`, `GAME_FINISHED` por el publicador after-commit de la 409. `GameView`: `salvos` y `sunk` en `me` y `opponent`; los `hits` de un salvo solo se revelan cuando el rival tambien disparo ese turno, y el rival nunca ve `ships`.
Verificacion: `./mvnw test` x3 -> 99 tests, 0 fallos, exit 0; prueba en vivo en el puerto 4704 (turno completo, eventos recibidos, proceso detenido). Decision: `sunk` de un jugador = sus barcos hundidos por salvos ya resueltos.
