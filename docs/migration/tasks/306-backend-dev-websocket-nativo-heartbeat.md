# 306 · backend-dev · WebSocket nativo (sin SockJS) y heartbeats STOMP
Estado: hecha
Fase: F3
Depende de: 305

Objetivo: el endpoint `/the-last-shipbender` pasa a WebSocket nativo y el broker envía/espera heartbeats cada 10 s (Cloudflare Tunnel corta las conexiones ociosas y así se detectan clientes caídos). Ver ADR 001 punto 4 y ADR 003.

Archivos: `config/websocket/WebSocketConfig.java`; `src/test/.../integration/GameplayWebSocketTest.java` (solo el transporte, excepción explícita de esta tarea).

Diseño (cerrado):
- `registerStompEndpoints`: `registry.addEndpoint("/the-last-shipbender");` sin `.withSockJS()`.
- `configureMessageBroker`: `config.enableSimpleBroker("/topic").setHeartbeatValue(new long[]{10000, 10000}).setTaskScheduler(messageBrokerTaskScheduler);`. El scheduler es el que ya crea Spring, inyectado con setter `@Autowired void setMessageBrokerTaskScheduler(@Lazy TaskScheduler s)` (patrón de la documentación de Spring; el `@Lazy` evita la dependencia circular).
- Test, método `connect(...)`: `new WebSocketStompClient(new StandardWebSocketClient())` en lugar de `SockJsClient`; borra los imports de SockJS y corrige el Javadoc ("Conecta por WebSocket nativo"). No cambies ninguna aserción.
- El front viejo deja de conectar hasta la 307 (misma rama, va justo después).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `grep -rn "SockJS\|SockJs\|sockjs" src/main/java src/test/java` → sin resultados.
- `grep -n "setHeartbeatValue" src/main/java/com/codeblockacademy/shipbender/config/websocket/WebSocketConfig.java` → 1 línea.
- Con `PORT=4704 ./mvnw spring-boot:run`: `curl -s -o /dev/null -w "%{http_code}" http://localhost:4704/the-last-shipbender/info` → distinto de `200` (el endpoint `/info` de SockJS ya no existe). Detén el proceso.

## Resultado
Endpoint nativo sin SockJS y broker con heartbeats 10 s/10 s (scheduler de Spring inyectado con `@Lazy`). Test: solo cambia el transporte de `connect(...)`.
- `./mvnw test`: 30 tests, 0 fallos. grep SockJS en main/test: sin resultados. `setHeartbeatValue`: 1 linea.
- `PORT=4704 spring-boot:run`: `/info` -> 404. Cliente WebSocket nativo (node) con JWT: `CONNECTED version:1.2 heart-beat:10000,10000`. Proceso detenido.
