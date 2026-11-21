# Plan de migración — The Last Shipbender

Lo edita solo el chat central. Cada fase se cierra con aprobación del usuario y merge de `migration/fN-*` a `main`.

## Estado de fases

| Fase | Rama | Estado | Criterio de cierre |
|---|---|---|---|
| F0 · Preparación | `migration/f0-preparacion` | **hecha** (mergeada a main) | Usuario aprueba plan y tareas de F1–F2 |
| F1 · Red de seguridad | `migration/f1-tests` | **hecha** (mergeada a main) | `./mvnw test` verde sobre Boot 2.6 sin tocar código de producción |
| F2 · Limpieza | `migration/f2-limpieza` | **hecha** (mergeada a main) | F1 verde; sin código muerto ni secretos en el código |
| F3 · Upgrade | `migration/f3-upgrade` | **hecha** (mergeada a main) | Boot 4.x + Java 21 + Postgres/Flyway + Testcontainers; F1 verde; app levanta |
| F4 · Arquitectura backend | `migration/f4-arquitectura` | **hecha** (mergeada a main) | Paquete `dev.racnar.shipbender`; paquetes por feature; dominio testeado sin Spring; contrato API v2 (ADR 004) implementado |
| F5 · Front nuevo | `migration/f5-frontend` | **hecha** (mergeada a main) | Partida completa jugable en React + TS |
| F6 · Deploy | `migration/f6-deploy` | en curso | Juego en el dominio vía Cloudflare Tunnel, con WebSocket |

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
| 401 | backend-dev | Renombre de paquete a `dev.racnar.shipbender` | F4 | hecha | — |
| 402 | backend-dev | Paquetes por feature + borrar `I*Service` | F4 | hecha | 401 |
| 403 | backend-dev | Dominio de flota y partida (Java puro) | F4 | hecha | 402 |
| 404 | qa | Tests del dominio sin Spring | F4 | hecha | 403 |
| 405 | qa | Tests REST v2 (quedan en rojo hasta 406-407) | F4 | hecha | 403 |
| 406 | backend-dev | REST v2: auth, jugadores, formato de error | F4 | hecha | 405 |
| 407 | backend-dev | REST v2: partidas y barcos | F4 | hecha | 406 |
| 408 | qa | Tests STOMP v2 (quedan en rojo hasta 409) | F4 | hecha | 407 |
| 409 | backend-dev | STOMP v2: eventos, emotes y errores | F4 | hecha | 408 |
| 410 | backend-dev | Limpieza final (V3 roles, etc.) | F4 | hecha | 409 |
| 411 | backend-dev | Dominio de salvos y turnos | F4 | hecha | 410 |
| 412 | qa | Tests de salvos | F4 | hecha | 411 |
| 413 | backend-dev | Salvos: REST y eventos | F4 | hecha | 412 |
| 501 | frontend-dev | Esqueleto Vite + Vitest + proxy | F5 | hecha | — |
| 502 | frontend-dev | Dominio TS puro | F5 | hecha | 501 |
| 503 | frontend-dev | Cliente API REST/STOMP y token (riesgo alto) | F5 | hecha | 501 |
| 504 | frontend-dev | Login, registro y rutas (riesgo alto) | F5 | hecha | 503 |
| 505 | frontend-dev | Lobby: nación, mapa, crear/unirse, ranking | F5 | hecha | 502, 504 |
| 506 | frontend-dev | Partida: sincronización y barcos | F5 | hecha | 505 |
| 507 | frontend-dev | Partida: salvos, fin, emotes, errores | F5 | hecha | 506 |
| 508 | central | Retirar front viejo (`git rm`; el usuario lo autorizó y lo ejecutó el central) | F5 | hecha | 501 |
| 509 | qa | Tests de rutas públicas del SPA | F5 | hecha | 508 |
| 510 | backend-dev | Front dentro del jar (perfil Maven) + seguridad (riesgo alto) | F5 | hecha | 509 |
| 511 | qa | E2E Playwright: partida completa | F5 | hecha | 507, 510 |
| 512 | reviewer | Revisión de seguridad F5 | F5 | hecha | 503, 504, 509, 510 |
| 513 | frontend-dev | STOMP relee el token y corta si el CONNECT es rechazado | F5 | hecha | 512 |
| 514 | backend-dev | CSP, favicon.svg y límites de entrada (riesgo alto) | F5 | hecha | 512 |
| 313 | frontend-dev | Corregir URL de logout del front viejo (`/api/players/logout`) | F3 | hecha | 311 |
| 205 | qa | Arreglar test intermitente de firma JWT manipulada | F2 | hecha | — |
| 203 | frontend-dev | Borrar código muerto front (sobras.js, delete/, TEST_DATA.js) | F2 | hecha | — |

Orden F5: 501 → (502 ∥ 503) → 504 → 505 → 506 → 507; en paralelo 508 → 509 → 510; luego 511 ∥ 512.
Orden de ejecución F1: 101 → (102 ∥ 105) → (103 ∥ 104). F2: (201 ∥ 203) → 202.

Línea base (F0): `./mvnw test` no ejecuta (falta `.mvn/wrapper/`; Lombok 1.18.22 no compila en JDK 21). Con Lombok 1.18.36: 3 tests triviales en verde.

## Decisiones

- [ADR 001 — Decisiones base de la migración](../adr/001-decisiones-base.md)
- [ADR 004 — Contrato API v2 (REST + STOMP), aprobado](../adr/004-contrato-api-v2.md)
- [ADR 006 — Front React + TS + Vite](../adr/006-frontend-react.md)
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

## Resultado F4

99 tests en verde (34 de dominio sin Spring/Docker + 11 de salvos sin Docker + integración REST/STOMP). Código: 81 → 54 archivos Java.
- Paquete `dev.racnar.shipbender` por feature (`player`, `game`, `chat`, `security`, `config`, `shared`); 8 interfaces `I*Service`, ModelMapper y la jerarquía `AirbenderException` borrados.
- Dominio rico en Java puro: `Cell`, `Fleet` (flota válida por construcción), `Game.join/placeShips/status/fire`, `PlayerStats`, `TurnResult`.
- Contrato v2 (ADR 004) implementado: REST con `ProblemDetail` (RFC 9457, `code` estable), STOMP con un topic público `/topic/games/{id}` sin posiciones de barcos, errores por usuario en `/user/queue/errors`, eventos de dominio publicados tras el commit.
- **Funcionalidad nueva (no existía en el original):** salvos, turnos simultáneos de 5 disparos y fin de partida (3/1/0, empate).
- Verificado en vivo (puerto 4704): eventos STOMP sin celdas, turno completo con dos jugadores, Swagger con las rutas del Anexo A (más salvos).
- Esquema: V2 (email único) y V3 (roles por nombre, limpieza).
Pendiente / no verificado:
- **El front viejo ya NO funciona** contra el contrato v2 (esperado; se reescribe en F5).
- `GAME_FINISHED` solo verificado por test de integración, no en vivo; rollback forzado tras publicar evento: razonado, no probado.
- Chat de texto: diseñado como v2.1 (ADR 004 Anexo F), sin implementar (decisión D3).
- Dev: el seeder puede no haber terminado cuando llega el primer login tras arrancar.
- Menores: `GamePlayerRepository.findByPlayerIdAndGameId` con parámetros en orden invertido (sin uso); `V1` aún admite `NONAME` en el check de `ship.type`.

## Resultado F5

Front nuevo en `/frontend` (React 19 + TypeScript + Vite + Vitest), empaquetable en el jar con `./mvnw -Pfrontend package`. 70 tests de front, 106 de backend, 1 E2E de Playwright (partida completa entre dos jugadores, 3 corridas verdes sobre un backend limpio). Front viejo (`static/`, 172 archivos) retirado.
- Estructura: `domain/` (reglas del juego en TS puro, 31 tests), `api/` (cliente REST/STOMP tipado, único módulo que toca el token), `auth/`, `screens/` (login, lobby, partida), `components/`. Sin librería de estado ni de rutas (rutas por hash).
- Verificado a mano por el central en el navegador: login/errores, sesión tras recarga, lobby (mapa, ranking sin emails), crear partida, **sincronización en vivo por WebSocket a través del proxy de Vite** (rival se une, flota completa → fase de disparo), partida completa de 4 turnos hasta "Ganaste", emote del rival (burbuja de ~3 s), token inválido → limpia y vuelve a login, y el front servido por el backend con la CSP activa (sin violaciones, WebSocket funcionando).
- Seguridad (tarea 512, reviewer): aprobado con menores, sin bloqueantes. Cerrados: STOMP con token caducado (513), CSP, favicon.svg y tope de longitud de contraseña/nombre (514).
Pendiente / no verificado:
- Token en `localStorage` en producción (decisión del usuario): mitigado por la CSP y la ausencia de HTML crudo; considerar bajar `JWT_EXPIRATION_MINUTES` en prod (24 h, sin revocación).
- `@Size(max=72)` cuenta caracteres, no bytes: una contraseña con muchos caracteres no ASCII puede superar los 72 bytes de bcrypt (esquina).
- `/v3/api-docs` da 401 (la regla pública es `/docs/**`): Swagger UI de dev probablemente no carga la spec; ya ocurría antes.
- Reconexión STOMP con token caducado: cubierta por tests con `Client` simulado, no probada con el WebSocket real.
- Los dos tableros comparten aria-labels de celda ("A1"...); se distinguen por el grupo. Mejorable para lectores de pantalla.
- **Sin diseño visual**: interfaz funcional con CSS mínimo (símbolos ▣ X • ?). El diseño visual real (skill `diseno-ui`) queda como trabajo aparte.
- Registro de usuario nuevo y "Unirse" desde la UI: cubiertos por el E2E, no a mano.
- Nota de entorno: si se cambia la versión de Node con `frontend/node/` instalado, hay que borrar esa carpeta.

## Tareas F6

Decisiones: ADR 007 (U1 shipbender.racnar.dev · U2 otra máquina · U4 backups fuera del repo · U5 JWT 480 min).
Orden: (601 ∥ 602) → 603 → 604 → (605 ∥ 606) → 607.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 601 | devops | Dockerfile multi-stage, `.dockerignore`, `.gitattributes` (LF) | F6 | pendiente | — |
| 602 | backend-dev | Cabeceras de proxy (`forward-headers-strategy`) y CSP `connect-src 'self'` (riesgo alto) | F6 | pendiente | — |
| 603 | devops | docker-compose (app, db, tunnel, backup) y `.env.example` (riesgo alto) | F6 | pendiente | 601, 602 |
| 604 | devops | Backups de Postgres y prueba de restauración (riesgo alto) | F6 | pendiente | 603 |
| 605 | devops | Runbook `docs/deploy/runbook.md` | F6 | pendiente | 604 |
| 606 | reviewer | Revisión de secretos, red y backups (riesgo alto) | F6 | pendiente | 601-604 |
| 607 | central + usuario | Puesta en marcha en la otra máquina y verificación | F6 | pendiente | 605, 606 |
