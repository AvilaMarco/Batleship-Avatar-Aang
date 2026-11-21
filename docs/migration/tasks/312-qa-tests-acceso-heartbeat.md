# 312 · qa · Tests de reglas de acceso HTTP y heartbeat STOMP
Estado: hecha
Fase: F3
Depende de: 311

Objetivo: dejar en tests lo que hoy solo se verifica con curl en la 306 y la 311, para que F4 no lo rompa sin enterarse.

Archivos: `src/test/java/com/codeblockacademy/shipbender/integration/SecurityRulesIntegrationTest.java` (nuevo), `integration/GameplayWebSocketTest.java`.

Tests (decididos):
- `SecurityRulesIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc` + `@Import(TestcontainersConfiguration.class)`), un `@ParameterizedTest` por grupo:
  - públicas sin token → no `403`: `GET /`, `GET /web/games.html`, `GET /webjars/stomp-websocket/2.3.4/stomp.min.js`, `GET /swagger-ui/index.html`;
  - protegidas sin token → `403`: `GET /api/games`, `GET /api/match/1`, `POST /api/players/nation/FIRE`;
  - no listadas → `403` incluso con token PLAYER (`AuthHelper`): `GET /api/inventado`, `GET /h2-console/`.
- `GameplayWebSocketTest`, un test nuevo: se conecta con token PLAYER y heartbeat de cliente `10000,10000` (`client.setDefaultHeartbeat(...)` + `ThreadPoolTaskScheduler` inicializado) y comprueba que el CONNECTED trae `heart-beat` `10000,10000`. Si `StompSession` no expone el header, usa un `StandardWebSocketClient` con `TextWebSocketHandler` que manda el frame `CONNECT` crudo (`accept-version:1.2`, `heart-beat:10000,10000`, `Authorization`) y verifica que la respuesta contiene `heart-beat:10000,10000`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 + los nuevos, 0 fallos, 3 corridas seguidas.
- Mutación manual (no se commitea): con `anyRequest().permitAll()` en `WebSecurityConfig` falla el grupo "no listadas"; sin `setHeartbeatValue` falla el test de heartbeat. Revierte y anótalo en el Resultado.

## Resultado
- `SecurityRulesIntegrationTest` (9 casos parametrizados: 4 publicas, 3 protegidas, 2 no listadas) y 1 test de heartbeat en `GameplayWebSocketTest`. Total 40 tests, 0 fallos.
- El heartbeat se lee del CONNECTED via `StompSessionHandler.afterConnected(session, connectedHeaders)` (`StompHeaders.getHeartbeat()`), no hizo falta frame crudo.
- Mutacion 1: `anyRequest().permitAll()` -> fallan los 2 casos de "no listadas" (404 en vez de 403). Revertida.
- Mutacion 2: quitar solo `setHeartbeatValue` NO falla el test: con `TaskScheduler` Spring ya usa 10000,10000 por defecto. Lo que si lo rompe es quitar `setTaskScheduler` y fijar `{0,0}` (llega 0,0). Revertida. El test protege el heartbeat efectivo, no la llamada literal.
- `./mvnw test -q` x3 seguidas: exit codes 0, 0, 0.
