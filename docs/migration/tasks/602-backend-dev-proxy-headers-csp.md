# 602 · backend-dev · Cabeceras de proxy para el WebSocket y CSP `connect-src 'self'`
Estado: hecha
Fase: F6
Depende de: —
Paralelizable: sí, con 601 (no comparten archivos). No con otra tarea que ejecute Maven.
Origen: ADR 007 puntos 6 y 7; pedido del usuario "WebSocket detrás del túnel… ¿llega el Host/Origin correcto?" y "CSP… ¿ajustar a wss del dominio?".
Riesgo: alto (cabeceras de seguridad y confianza en el proxy) → revisión en 606

Objetivo: detrás de cloudflared (TLS terminado afuera, `X-Forwarded-Proto: https`) el handshake WebSocket pasa la comprobación de mismo origen, y la CSP deja de permitir WebSocket hacia cualquier host.

Archivos: `src/main/resources/application.properties`, `src/main/java/dev/racnar/shipbender/security/WebSecurityConfig.java`; tests: añadir casos en `src/test/.../integration/SecurityRulesIntegrationTest.java` y un test de handshake WebSocket nuevo (o en la clase de tests STOMP que ya existe; excepción explícita: solo AÑADIR casos, no editar los existentes).

Diseño (cerrado):
- `application.properties`: `server.forward-headers-strategy=native`, con un comentario de una línea con el porqué (cloudflared termina TLS; Tomcat solo confía en IPs privadas).
- `WebSecurityConfig.java:34`: `connect-src 'self' ws: wss:` → `connect-src 'self'`. El resto de la política no cambia.
- Sin `setAllowedOrigins`/`setAllowedOriginPatterns` en `WebSocketConfig`.

Criterios de aceptación:
- Tests nuevos, primero en rojo (anotar en el Resultado cómo fallaron):
  1. Handshake WebSocket a `/the-last-shipbender` con `Origin: https://localhost` y `X-Forwarded-Proto: https` → conecta (sin la propiedad da 403).
  2. Handshake con `Origin: https://evil.example` y `X-Forwarded-Proto: https` → rechazado (403 o fallo del handshake).
  3. La CSP de `GET /` contiene `connect-src 'self';` y no contiene `ws:`.
- `./mvnw clean test` → verde x2.
- En vivo, solo si el 4704 está libre: `PORT=4704 ./mvnw -Pfrontend spring-boot:test-run`, abrir `http://localhost:4704/`, iniciar sesión y entrar a una partida: en la consola del navegador no hay violaciones de CSP y el WebSocket queda `101`. Detener por PID.
- Commit `fix(seguridad): forward headers para WebSocket tras proxy y CSP connect-src 'self' (tarea 602)`.

## Resultado
- `application.properties`: `server.forward-headers-strategy=native` (+ comentario). `WebSecurityConfig.java:34`: `connect-src 'self'`. Sin cambios en `WebSocketConfig`.
- Tests nuevos: `WebSocketOriginBehindProxyTest` (2) y `csp_connectSrcIsSelfOnly` en `SecurityRulesIntegrationTest`.
- Rojo: CSP -> assert falla (contenia `ws: wss:`); handshake https mismo origen -> 403 "did not permit the HTTP upgrade". Origen ajeno -> 403 ya antes (es guarda, no rojo). Con `-Dserver.forward-headers-strategy=none` el test de conexion vuelve a 403.
- Causa raiz a tener en cuenta: con `X-Forwarded-Proto: https` el RemoteIpValve de Tomcat fija el puerto 443, asi que el Origin debe ir sin puerto (como el de cloudflared, `https://dominio`). Un Origin `https://localhost:<puerto>` da 403. `native` funcionó en test (el cliente es 127.0.0.1/::1, proxy interno); no hizo falta el plan B.
- Verde: `./mvnw clean test` x2 -> 109 tests, 0 fallos, BUILD SUCCESS ambas.
- En vivo (puerto 4704, `spring-boot:test-run` sin `-Pfrontend`, detenido por PID): CSP de `/` con `connect-src 'self';`; handshake curl con Host racnar.test + Origin https://racnar.test + XFP https -> 101; Origin https://evil.example -> 403.
- Pendiente de verificar (chat central): consola del navegador sin violaciones de CSP y WebSocket 101 en una partida real.
- Riesgo anotado: Safari < 15.4 puede no tratar `'self'` como ws/wss; no se añadió `ws:/wss:`. Si cloudflared llegara desde una IP no privada (otro contenedor en red publica), `native` no confiaria en la cabecera: en compose la IP es privada.
