# 305 · backend-dev · Seguridad WebSocket con la API de Security 6 (sin la clase base deprecada)
Estado: hecha
Fase: F3
Depende de: 304

Objetivo: reemplazar `AbstractSecurityWebSocketMessageBrokerConfigurer` (deprecado en Security 6, borrado en 7) antes de pasar a Boot 4. Las reglas STOMP no cambian. Ver ADR 003.

Archivos: `config/websocket/WebSocketSecurityConfig.java`, `config/websocket/WebSocketAuthenticationSecurityConfig.java` (se borra).

Diseño (cerrado):
- No uses `@EnableWebSocketSecurity`: siempre exige token CSRF en el CONNECT y no tiene equivalente a `sameOriginDisabled()`. Acá no hace falta CSRF porque la autenticación va por JWT en un header STOMP, no por cookie. Se usa la configuración manual que indica la documentación de Spring Security ("Disabling CSRF within WebSockets").
- `WebSocketSecurityConfig` pasa a ser `@Configuration` que implementa `WebSocketMessageBrokerConfigurer` (sin `@EnableWebSecurity`, que estaba duplicado) y:
  - recibe por constructor `AuthChannelInterceptorAdapter` y `ApplicationContext`;
  - en `configureClientInboundChannel` registra, en este orden: `authChannelInterceptorAdapter`, `new SecurityContextChannelInterceptor()` y un `AuthorizationChannelInterceptor` con `SpringAuthorizationEventPublisher`;
  - las reglas se arman con `MessageMatcherDelegatingAuthorizationManager.builder()`: `nullDestMatcher().authenticated()`, `simpDestMatchers("/app/**").hasAuthority("PLAYER")`, `simpSubscribeDestMatchers("/topic/**").hasAuthority("PLAYER")`, `anyMessage().denyAll()`.
- Borrar `WebSocketAuthenticationSecurityConfig` (el `@Order(HIGHEST_PRECEDENCE + 99)` ya no hace falta: el orden queda explícito en una sola lista).
- `WebSocketConfig` no se toca.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos (incluye `GameplayWebSocketTest`: CONNECT sin token rechazado, con token PLAYER acepta).
- `grep -rn "AbstractSecurityWebSocketMessageBrokerConfigurer\|sameOriginDisabled\|WebSocketAuthenticationSecurityConfig" src/main` → sin resultados.
- `grep -rn "@EnableWebSecurity" src/main/java` → solo `WebSecurityConfig.java`.

## Resultado
La API `MessageMatcherDelegatingAuthorizationManager.builder()` de Security 6.5.11 compila tal cual el diseño (nullDestMatcher/simpDestMatchers/simpSubscribeDestMatchers/anyMessage + `.build()`), sin adaptaciones.
`WebSocketSecurityConfig` ahora implementa `WebSocketMessageBrokerConfigurer` con los 3 interceptores en orden explícito; `WebSocketAuthenticationSecurityConfig` borrada.
Verificado: `./mvnw test` → 30 tests, 0 fallos (GameplayWebSocketTest 3/3); los dos greps de criterios sin resultados salvo `@EnableWebSecurity` solo en WebSecurityConfig; sin procesos java.
