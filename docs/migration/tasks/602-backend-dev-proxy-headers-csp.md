# 602 · backend-dev · Cabeceras de proxy para el WebSocket y CSP `connect-src 'self'`
Estado: pendiente
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
