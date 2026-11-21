# 408 · qa · Tests del contrato STOMP v2: eventos sin barcos, emotes y errores (test primero)
Estado: hecha
Fase: F4
Depende de: 407
Paralelizable: no (la 409 se valida contra estos tests)

Objetivo: fijar ADR 004 Anexo B con tests de WebSocket. **Quedan en rojo** hasta la 409. El hallazgo de F3 (el rival recibe tus barcos) pasa a ser un test que lo prohíbe.

Archivos: `src/test/java/dev/racnar/shipbender/integration/GameplayWebSocketTest.java` (renombrar a `GameEventsWebSocketTest.java`).

Casos (cerrados). Reutilizar `connect`, `subscribe` (espera con deadline) y `AuthHelper`. Topic `/topic/games/{id}`.
- B se une con A suscripto → A recibe `PLAYER_JOINED` con `player.id` de B y sin `email`.
- A coloca barcos por REST con B suscripto → B recibe `SHIPS_PLACED` con `player_id` de A; **el texto del mensaje no contiene ninguna celda de A** (`"A1"`, `"cells"`, `"ships"`). Reemplaza `sendShips_validShips_..._toHostAndRival` (que afirmaba la fuga).
- B coloca después → ambos reciben `SHIPS_PLACED` y luego `GAME_STARTED` con `turn=1`, en ese orden.
- Flota inválida por REST → 400 y **ningún** evento en el topic en 1 s.
- Emote: A envía `SEND /app/games/{id}/emotes {"emote":"emote-1"}` (JSON) → A y B reciben `EMOTE` con `player_id` de A.
- Emote de un ajeno C (suscripto a `/user/queue/errors`) → C recibe `ProblemDetail` con `code=NOT_A_PARTICIPANT` e `instance` = destino; A no recibe nada.
- Emote vacío → `/user/queue/errors` con `code=VALIDATION_FAILED`.
- SUBSCRIBE a `/topic/gameplay/{id}/ships` (v1) → denegado (frame ERROR o conexión cerrada; afirmar lo observado).
- `connectWithoutToken_...` y heartbeat: sin cambios.
- Si el usuario aprueba el chat (decisión abierta D3): `SEND /app/games/{id}/chat {"text":"hola"}` → `CHAT_MESSAGE`; texto de 201 caracteres → `VALIDATION_FAILED`.

Criterios de aceptación:
- `grep -n "fuga\|knownBug\|/app/\" + id + \"/ships" src/test -r` → sin salida.
- `./mvnw test`: todo lo de REST y unitarios en verde; los nuevos de STOMP en rojo. Lista exacta en el Resultado.
- Commit `test: contrato STOMP v2 (tarea 408, rojo hasta 409)`.

## Resultado
Renombrado `GameplayWebSocketTest` a `GameEventsWebSocketTest` (10 tests; chat D3 no incluido por decision del usuario). `./mvnw test`: 78 tests, 71 verdes, 7 rojos (todos en `GameEventsWebSocketTest`). REST, unitarios y los 2 de conexion (sin token, heartbeat) en verde.

Rojos a proposito hasta la 409 (el evento/destino v2 aun no existe, salvo la ultima fila):
- `playerJoins_hostReceivesPlayerJoinedWithoutEmail`: no llega PLAYER_JOINED.
- `shipsPlaced_rivalReceivesShipsPlacedWithoutAnyCell`: no llega SHIPS_PLACED.
- `bothPlaceShips_bothReceiveShipsPlacedThenGameStartedWithTurn1`: no llega ningun evento.
- `emote_participantSends_bothReceiveEmoteWithSenderId`: no llega EMOTE.
- `emote_fromNonParticipant_errorQueueGetsNotAParticipant_andHostGetsNothing` y `emote_blank_errorQueueGetsValidationFailed`: rojos en `subscribe` a `/user/queue/errors` (hoy `anyMessage().denyAll()` lo rechaza). Riesgo: el helper `countSubs` asume que el registro reescribe `/user/queue/errors` a `/queue/errors-user...`; no verificable hasta la 409.
- `subscribeToV1ShipsTopic_isDenied`: rojo por hallazgo real, no por evento faltante: hoy `/topic/**` esta permitido para PLAYER, asi que la suscripcion v1 NO se deniega. La 409 debe restringir los SUBSCRIBE a `/topic/games/*` y `/user/queue/errors`.
En verde ya (sin cambios): `invalidFleet_is400AndNoEventIsPublished` (aun no se publica nada).
