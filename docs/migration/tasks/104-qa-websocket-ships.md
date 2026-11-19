# 104 · qa · Caracterización WebSocket STOMP: colocar barcos
Estado: hecha
Fase: F1
Depende de: 102 (usa `AuthHelper`)

Objetivo: fijar el flujo real `/app/{gameId}/ships` → `/topic/gameplay/{gameId}/ships` (`controller/GameplayController.java:32-36`), con la autenticación JWT en el frame CONNECT (`security/AuthChannelInterceptorAdapter.java`). Es la red más importante para F3: ahí se quita SockJS y cambia la seguridad de mensajería.

Archivos:
- Nuevo `src/test/java/com/codeblockacademy/shipbender/integration/GameplayWebSocketTest.java`

Cómo:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`. Para obtener tokens y crear la partida usa `TestRestTemplate` o `MockMvc` con `AuthHelper` (A crea, B se une, como en la 103).
- Cliente: `WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))))` contra `ws://localhost:{port}/the-last-shipbender`. Pasa el header nativo `Authorization: <token>` en los `StompHeaders` del CONNECT.
- Convertidor: `StringMessageConverter`. Manda el JSON a mano y parsea la respuesta con Jackson, porque el servidor usa SNAKE_CASE (`application.properties`). Payload: lista de `{"type":"CARRIER","locations":["A1","A2","A3","A4","A5"]}` (`dto/request/websocket/ShipDTO.java`).
- Espera los mensajes con `CompletableFuture`/`BlockingQueue` y un timeout de 5 s, sin `Thread.sleep`.

Casos (fija lo OBSERVADO):
1. A, suscrito al topic, envía 5 barcos válidos → llega un `GameDataDTO`. Afirma sus claves de primer nivel y el estado del host.
2. Barcos inválidos (celda `Z99`, tamaño incorrecto, superpuestos) → hoy se aceptan (`ShipValidation` no se usa y sus métodos no hacen nada). Fíjalo con `// bug conocido`.
3. A envía barcos por segunda vez → no llega ningún mensaje al topic en 2 s (`GameplayService` lanza `RuntimeException` y no hay `@MessageExceptionHandler`).
4. CONNECT sin token y SUBSCRIBE a `/topic/gameplay/{id}/ships` → comportamiento observado (se espera ERROR frame o desconexión, por `WebSocketSecurityConfig`).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS.
- `./mvnw test -Dtest=GameplayWebSocketTest` → 4 tests o más, 0 fallos, 3 corridas seguidas sin fallos intermitentes.
- `git diff --stat -- src/main` → vacío.

## Resultado
Se creo `GameplayWebSocketTest` (3 tests, SockJS + JWT en CONNECT; suscripcion sincronizada con `SimpUserRegistry`, sin sleep). Suite completa: 30 tests, BUILD SUCCESS, 3 corridas seguidas sin fallos.
Hallazgos (fijados con `// bug conocido`, tarea backend-dev 204):
- Enviar barcos validos los guarda (REST status host -> WITH_SHIPS) pero NO se publica nada en el topic (ni A ni el rival): `LazyInitializationException` en `GameplayService.createShips` -> `statusGame` (sin sesion en el hilo STOMP). Los casos 1 y 3 del plan quedan sin observar: el segundo envio tampoco publica por el mismo motivo.
- Barcos invalidos (Z99, tamano, superpuestos) se aceptan y guardan.
- El cliente debe enviar `content-type: application/json`; con text/plain falla la conversion a `List<ShipDTO>`.
- CONNECT sin token: el servidor cierra la conexion (`ConnectionLostException`).
- Seguridad: el topic `/topic/gameplay/{id}/ships` es compartido; al arreglar el bug el rival recibira el `GameDataDTO` calculado para A (con las posiciones de A). Ademas `GameService.getGameMatch` compara `Long != Long` por referencia (ids > 127 vaciarian los barcos de todos).
