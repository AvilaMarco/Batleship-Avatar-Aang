# Plan de migración — The Last Shipbender

Lo edita solo el chat central. Cada fase se cierra con aprobación del usuario y merge de `migration/fN-*` a `main`.

## Estado de fases

| Fase | Rama | Estado | Criterio de cierre |
|---|---|---|---|
| F0 · Preparación | `migration/f0-preparacion` | **hecha** (mergeada a main) | Usuario aprueba plan y tareas de F1–F2 |
| F1 · Red de seguridad | `migration/f1-tests` | **hecha** (mergeada a main) | `./mvnw test` verde sobre Boot 2.6 sin tocar código de producción |
| F2 · Limpieza | `migration/f2-limpieza` | **hecha** (mergeada a main) | F1 verde; sin código muerto ni secretos en el código |
| F3 · Upgrade | `migration/f3-upgrade` | **hecha** (mergeada a main) | Boot 4.x + Java 21 + Postgres/Flyway + Testcontainers; F1 verde; app levanta |
| F4 · Arquitectura backend | `migration/f4-arquitectura` | en curso | Paquete `dev.racnar.shipbender`; paquetes por feature; dominio testeado sin Spring; contrato API v2 (ADR 004) implementado |
| F5 · Front nuevo | `migration/f5-frontend` | pendiente | Partida completa jugable en React + TS |
| F6 · Deploy | `migration/f6-deploy` | pendiente | Juego en el dominio vía Cloudflare Tunnel, con WebSocket |

Paralelismo permitido: F5 en paralelo con F4 una vez congelado el contrato de API; Dockerfile (F6) desde el fin de F3.

## Tareas

Detalle en `tasks/`. Terminadas se mueven a `tasks/done/`.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 101 | backend-dev | Build reproducible: wrapper + Lombok compatible con JDK 21 | F1 | hecha | — |
| 102 | qa | Auth: login, registro, JWT y `AuthHelper` | F1 | hecha | 101 |
| 103 | qa | REST de games y match | F1 | hecha | 102 |
| 104 | qa | WebSocket STOMP `/app/{id}/ships` | F1 | hecha | 102 |
| 105 | qa | Arreglar MatchServiceTest (Mockito) + tests de GameValidation | F1 | hecha | 101 |
| 201 | backend-dev | Borrar código muerto backend (stubs, ShipValidation, jjwt, devtools duplicado) | F2 | hecha | F1 |
| 202 | backend-dev | Secreto/expiración JWT a config externa, perfiles dev/prod, h2-console off en prod | F2 | hecha | 201, 102 |
| 204 | backend-dev | Arreglar `LazyInitializationException` en `createShips` (`@Transactional`); lo de `Long !=` no era bug | F2 | hecha | 201 |
| 301 | backend-dev | Limpiar pom (mysql, plugin springdoc, start/stop) | F3 | hecha | — |
| 302 | backend-dev | Boot 2.7.18 + `SecurityFilterChain` | F3 | hecha | 301 |
| 303 | backend-dev | Boot 3.5.16: jakarta, Security 6, Hibernate 6, springdoc 2.8 | F3 | hecha | 302 |
| 304 | backend-dev | Java 21 | F3 | hecha | 303 |
| 305 | backend-dev | Seguridad WebSocket con API de Security 6 | F3 | hecha | 304 |
| 306 | backend-dev | WebSocket nativo (sin SockJS) + heartbeats | F3 | hecha | 305 |
| 307 | frontend-dev | Front viejo con `Stomp.client` y webjar con versión | F3 | hecha | 306 |
| 308 | backend-dev | Boot 4.2.0: starters modulares, Jackson 3, Hibernate 7, springdoc 3.2 | F3 | hecha | 307 |
| 309 | qa | Testcontainers Postgres | F3 | hecha | 308 |
| 310 | backend-dev | Flyway V1, `ddl-auto=validate`, borrar H2, seed solo en dev | F3 | hecha | 309 |
| 311 | backend-dev | Deny-by-default con lista cerrada de rutas públicas | F3 | hecha | 310 |
| 312 | qa | Tests de reglas de acceso y heartbeat | F3 | hecha | 311 |
| 401 | backend-dev | Renombre de paquete a `dev.racnar.shipbender` | F4 | pendiente | — |
| 402 | backend-dev | Paquetes por feature + borrar `I*Service` | F4 | pendiente | 401 |
| 403 | backend-dev | Dominio de flota y partida (Java puro) | F4 | pendiente | 402 |
| 404 | qa | Tests del dominio sin Spring | F4 | pendiente | 403 |
| 405 | qa | Tests REST v2 (quedan en rojo hasta 406-407) | F4 | pendiente | 403 |
| 406 | backend-dev | REST v2: auth, jugadores, formato de error | F4 | pendiente | 405 |
| 407 | backend-dev | REST v2: partidas y barcos | F4 | pendiente | 406 |
| 408 | qa | Tests STOMP v2 (quedan en rojo hasta 409) | F4 | pendiente | 407 |
| 409 | backend-dev | STOMP v2: eventos, emotes y errores | F4 | pendiente | 408 |
| 410 | backend-dev | Limpieza final (V3 roles, etc.) | F4 | pendiente | 409 |
| 411 | backend-dev | Dominio de salvos y turnos | F4 | pendiente | 410 |
| 412 | qa | Tests de salvos | F4 | pendiente | 411 |
| 413 | backend-dev | Salvos: REST y eventos | F4 | pendiente | 412 |
| 313 | frontend-dev | Corregir URL de logout del front viejo (`/api/players/logout`) | F3 | hecha | 311 |
| 205 | qa | Arreglar test intermitente de firma JWT manipulada | F2 | hecha | — |
| 203 | frontend-dev | Borrar código muerto front (sobras.js, delete/, TEST_DATA.js) | F2 | hecha | — |

Orden de ejecución F1: 101 → (102 ∥ 105) → (103 ∥ 104). F2: (201 ∥ 203) → 202.

Línea base (F0): `./mvnw test` no ejecuta (falta `.mvn/wrapper/`; Lombok 1.18.22 no compila en JDK 21). Con Lombok 1.18.36: 3 tests triviales en verde.

## Decisiones

- [ADR 001 — Decisiones base de la migración](../adr/001-decisiones-base.md)
- [ADR 004 — Contrato API v2 (REST + STOMP), aprobado](../adr/004-contrato-api-v2.md)
- [ADR 005 — Arquitectura backend por feature con dominio rico](../adr/005-arquitectura-backend.md)
- [ADR 003 — Upgrade de Spring Boot (ruta 2.6→2.7→3.5→Java 21→4.2)](../adr/003-upgrade-spring-boot.md)
- [ADR 002 — Paquete base, Testcontainers, estilo de arquitectura y contrato v2](../adr/002-paquete-tests-arquitectura-contrato.md)

Orden previsto al inicio de F4: renombre de paquete → ADR 004 contrato v2 (arquitecto) → reorganización por feature → implementación v2 → qa actualiza tests del contrato viejo.

## Pendiente del usuario

- Renombrar `master` → `main` en GitHub (Settings → Branches) y luego `git push -u origin main`. Local ya está en `main`.

## Resultado F1

30 tests en verde (3 corridas seguidas), `src/main` sin cambios. Cubren auth/JWT, REST games y match, WebSocket de barcos y validaciones.
Vigilancia: 1 fallo intermitente de `AuthIntegrationTest` reportado una vez y no reproducido en ~11 corridas posteriores.
Bugs fijados como "conocidos" en tests: unirse a la propia partida (`MatchService.java:58`), barcos inválidos aceptados, error engañoso "Player Already in Game", `LazyInitializationException` en WebSocket (tarea 204), JWT con firma inválida sin respuesta controlada.

## Resultado F2

30 tests en verde. Secreto JWT fuera del código (config por perfil, prod falla si falta `JWT_SECRET`), expiración 24 h, h2-console solo en dev, jjwt y stubs de `SalvoController` borrados, WebSocket de barcos publica el mensaje.
Borrados del front hechos por el usuario (denegados a los agentes por permisos). `.gitignore` ampliado. Puertos de desarrollo: 4703 (main) / 4704-4705 (pruebas), ver `CLAUDE.md`. Test intermitente de firma JWT arreglado (tarea 205; causa: el último carácter base64url solo aporta 4 bits).
Pendiente para prod (F6): definir un `JWT_SECRET` nuevo; el viejo está en el historial de git y se considera comprometido.
Conocido para F4: el rival recibe los barcos de A por el topic compartido; sin validación de barcos; sin `@MessageExceptionHandler`.

## Resultado F3

Spring Boot 4.2.0 / Java 21 / Hibernate 7 / Security 7 / Jackson 3. Postgres 17 + Flyway (V1 desde las entidades, `ddl-auto=validate`), H2 eliminado, Testcontainers (un Postgres por contexto de test; los tests necesitan Docker). WebSocket nativo con heartbeats de 10 s, seguridad de mensajería explícita, HTTP deny-by-default. 40 tests en verde.
Verificado a mano en el navegador integrado (puerto 4704): login, mapa, `game.html` con todos sus assets, conexión STOMP nativa (CONNECTED, heart-beat 10000,10000, PING/PONG) y mensaje recibido en el topic.
Hallazgos para fases siguientes (ver APRENDIZAJE.md):
- **F4 (seguridad, confirmado en vivo):** el topic `/topic/gameplay/{id}/ships` es compartido: el jugador A recibió en su navegador las posiciones de los barcos del rival B.
- **F5 (front, ya existente en el original):** `game.js:99` `updataHostAndClientData` revienta si el creador abre la partida antes de que haya rival (`CLIENT` undefined) y el socket ni se conecta; `handlerShips` → `createShipGrid` revienta con los barcos del rival (`update_html.js:93`).
- No verificado: arranque sin Docker (mensaje de error de Testcontainers); `spring-boot:run` contra una BD real (F6); `JWT_SECRET` de prod (F6).
