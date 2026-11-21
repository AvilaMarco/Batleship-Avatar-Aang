# 409 · backend-dev · STOMP v2: eventos after-commit, emotes y `/user/queue/errors`
Estado: borrador
Fase: F4
Depende de: 408
Paralelizable: no

Objetivo: implementar ADR 004 Anexo B con el mecanismo de ADR 005 punto 7, hasta `./mvnw test` todo en verde.

Archivos (`P = src/main/java/dev/racnar/shipbender`): nuevos `P/game/event/GameEvent.java`, `P/game/event/GameEventStompPublisher.java`, `P/chat/EmoteController.java`, `P/chat/ChatService.java`, `P/chat/dto/EmoteRequest.java`, `P/shared/error/StompExceptionHandler.java`; modificados `P/game/GameService.java`, `P/config/WebSocketConfig.java`, `P/security/WebSocketSecurityConfig.java`.

Diseño (cerrado):
- `GameEvent`: `sealed interface` con records `PlayerJoined(long gameId, PlayerSummary player)`, `ShipsPlaced(long gameId, long playerId)`, `GameStarted(long gameId, int turn)`, `EmoteSent(long gameId, long playerId, String emote)`; `@JsonTypeInfo(use = NAME, property = "type")` + `@JsonSubTypes` con los nombres `PLAYER_JOINED`, `SHIPS_PLACED`, `GAME_STARTED`, `EMOTE`. Método `gameId()` en la interfaz.
- `GameService` publica con `ApplicationEventPublisher`: `join` → `PlayerJoined`; `placeShips` → `ShipsPlaced` y, si `game.status() == IN_GAME`, `GameStarted`.
- `GameEventStompPublisher`: `@TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)` → `messagingTemplate.convertAndSend("/topic/games/" + e.gameId(), e)`. Es el único lugar que conoce destinos `/topic`.
- `EmoteController`: `@MessageMapping("/games/{gameId}/emotes")`, `@DestinationVariable long gameId`, `@Payload @Valid EmoteRequest`, `Principal`. `ChatService.sendEmote(email, gameId, emote)`: valida participante con `game.participant(...)` (transacción de solo lectura) y publica `EmoteSent` (no persiste). `EmoteRequest(@NotBlank @Size(max = 32) String emote)`.
- `StompExceptionHandler`: `@ControllerAdvice` con `@MessageExceptionHandler` + `@SendToUser(destinations = "/queue/errors", broadcast = false)` → `ProblemDetail` con `code` (`ShipbenderException` → su código; `MethodArgumentNotValidException` de messaging → `VALIDATION_FAILED`; resto → `INTERNAL_ERROR`) e `instance` = destino del mensaje (header `simpDestination`).
- `WebSocketConfig`: `enableSimpleBroker("/topic", "/queue")` (heartbeat y scheduler igual).
- `WebSocketSecurityConfig`: `nullDestMatcher().authenticated()`, `simpDestMatchers("/app/games/*/emotes").hasAuthority("PLAYER")`, `simpSubscribeDestMatchers("/topic/games/*", "/user/queue/errors").hasAuthority("PLAYER")`, `anyMessage().denyAll()`.
- `AuthChannelInterceptorAdapter` → `StompAuthInterceptor`; si el JWT no verifica, no setea usuario (el CONNECT se rechaza como hoy, sin excepción no controlada).
- Si el usuario aprueba el chat (D3): `@MessageMapping("/games/{gameId}/chat")`, `ChatRequest(@NotBlank @Size(max = 200) String text)`, evento `ChatMessage(gameId, playerId, text, sentAt)` tipo `CHAT_MESSAGE`, destino agregado a la regla de `/app`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 0 fallos, 0 errores.
- `grep -rn "convertAndSend\|@SendTo(" src/main/java` → solo en `GameEventStompPublisher`.
- `grep -rn "@RequestBody" src/main/java/dev/racnar/shipbender/chat` → sin salida.
- Commit `feat: eventos STOMP v2, emotes y errores por usuario (tarea 409)`.

## Resultado
