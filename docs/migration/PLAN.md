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
| F6 · Deploy | `migration/f6-deploy` | **construido; falta la puesta en marcha (607) por el usuario** | Juego en el dominio vía Cloudflare Tunnel, con WebSocket |
| F7 · Endurecimiento y ajustes de deploy | `migration/f7-*` | **hecha y mergeada** (falta el `recibir` real del servidor) | Contraseñas, deploy adaptado al servidor, revisión 621 corregida en 622 |
| F8 · Mapa, diseño conectado, pantallas | `migration/f8-*` | **hecha y mergeada** | Mapa SVG, refresco, LOTUS, perfil, registro, PWA |

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
Orden: (601 ∥ 602) → 603 → 604 → (605 ∥ 606) → 608 → 607.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 601 | devops | Dockerfile multi-stage, `.dockerignore`, `.gitattributes` (LF) | F6 | hecha | — |
| 602 | backend-dev | Cabeceras de proxy (`forward-headers-strategy`) y CSP `connect-src 'self'` (riesgo alto) | F6 | hecha | — |
| 603 | devops | docker-compose (app, db, tunnel, backup) y `.env.example` (riesgo alto) | F6 | hecha | 601, 602 |
| 604 | devops | Backups de Postgres y prueba de restauración (riesgo alto) | F6 | hecha | 603 |
| 605 | devops | Runbook `docs/deploy/runbook.md` | F6 | hecha | 604 |
| 606 | reviewer | Revisión de secretos, red y backups (riesgo alto) | F6 | hecha | 601-604 |
| 608 | devops | Endurecimiento tras la revisión 606 (umask, .env.example, dockerignore, runbook) | F6 | hecha | 605, 606 |
| 607 | central + usuario | Puesta en marcha en la otra máquina y verificación | F6 | pendiente | 605, 606 |

## Resultado F6 (hasta la 607)

Hecho y verificado en un proyecto Docker aislado (`shipbender-test`) en esta máquina: Dockerfile de tres etapas (no root, perfil `prod` fijado, sin devtools), `docker-compose.yml` (db, app, tunnel, backup; sin ningún `ports:`; db y backup en red interna; `cloudflared:2026.11.1` fijado), `.env.example` sin secretos, backups con retención (14 días) y `restore.sh` atómico con validación previa y dump de seguridad, runbook `docs/deploy/runbook.md` con cada comando probado (incluido actualizar → rollback), cabeceras de proxy para el WebSocket y CSP `connect-src 'self'` (probada en el navegador). 109 tests de backend verdes.
Revisión de seguridad (606): aprobado con condiciones, sin bloqueantes; los menores cerrados en la 608.
**Falta (607, la hace el usuario con el central):**
- Decir el SO de la otra máquina (+ Docker/Compose, RAM, disco para backups) para completar el bloque por SO del runbook.
- Crear el túnel y el hostname `shipbender.racnar.dev` → `http://app:8080` en Cloudflare, Always Use HTTPS + Full (strict) + WebSockets, y la **regla de rate limiting obligatoria** (login y registro).
- En el servidor: clonar `main`, rellenar `.env` con secretos nuevos (`openssl rand -base64 48`; JWT_SECRET nuevo, el antiguo está en el historial de git), `docker compose up -d --build`.
- Verificar desde fuera (criterios de la tarea 607), incluido el reinicio de la máquina.
No verificado: el túnel real, el rollback con una migración de Flyway nueva, Ctrl-C a mitad de `restore.sh`, Linux/systemd.
Backlog: contraseña mínima de 8 caracteres (hoy acepta 1; rompe tests y seed: hacerlo con qa), perfil por defecto `dev` falla abierto si se usa `docker run` suelto, fusionar `master`→`main` en GitHub.

## Fase F7 · Endurecimiento y diseño (en curso)

Rama `migration/f7-endurecimiento`. Parte 1 (esta): contraseña mínima de 8 caracteres al registrarse (hallazgo de la revisión 606). Parte 2: diseño visual del front (skill `diseno-ui`), se planifica al terminar la parte 1.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 701 | qa | Tests: contraseña mínima de 8 caracteres (rojo hasta 702) | F7 | hecha | — |
| 702 | backend-dev | Registro: `@Size(min=8, max=72)` y límite de 72 bytes | F7 | hecha | 701 |
| 703 | frontend-dev | Registro: ayuda y `minLength` en el formulario | F7 | hecha | 702 |
| 704 | qa | Test: contraseña de más de 72 bytes → 400 | F7 | hecha | 702 |
| 705 | qa | Test: el error de bytes llega bajo la clave `password` (rojo hasta 706) | F7 | hecha | 704 |
| 706 | backend-dev | Restricción propia `MaxUtf8Bytes` sobre el campo | F7 | hecha | 705 |

Resultado F7 parte 1: registro exige contraseña de 8 a 72 caracteres y ≤ 72 bytes UTF-8 (restricción propia `@MaxUtf8Bytes` sobre el campo, mensaje en español bajo `errors.password`); el login no cambia (el seed `123` sigue entrando). 112 tests de backend y 73 de front verdes. Descubierto probando: con `@Size(max=72)` una contraseña de 40 caracteres de 2 bytes pasaba la validación y bcrypt lanzaba un 500. Pendiente: parte 2 (diseño visual del front); el error de contraseña se muestra en la alerta general, no junto al campo.

### F7 · Ajustes de deploy (servidor real: Debian 13 con Caddy, túnel existente)

Decisiones (2026-11-22): compilar en la PC de desarrollo y enviar la imagen por SSH (`docker save | gzip | ssh ... docker load`); sin túnel propio (el del compose pasa a `profile tunnel`, desactivado por defecto); la app publica solo en `127.0.0.1:APP_PORT` (lo asigna el administrador del servidor) y Caddy reparte; el servidor no tiene salida a Internet salvo el túnel, así que la imagen de Postgres también se envía.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 609 | devops | `start_period` en el healthcheck de db | F7 | hecha | — |
| 610 | devops | Compose: puerto en loopback y túnel opcional (riesgo alto) | F7 | hecha | 609 |
| 611 | devops | `ship.sh` y `deploy.sh` (envío, backup, healthcheck, rollback) (riesgo alto) | F7 | parcial (falta SSH real) | 610 |
| 612 | reviewer | Revisión: loopback, SSH restringido, despliegue | F7 | hecha (1 bloqueante) | 610, 611 |
| 613 | devops | Endurecimiento: rollback ante corte de SSH, lock, topes, memswap (riesgo alto) | F7 | parcial (falta sshd real y flock en Debian) | 612 |

### Resultado F7 · ajustes de deploy (rama `migration/f7-deploy-ajustes`, sin mergear)

Construido y probado en Docker local aislado: `start_period` en el healthcheck de db (609), compose con la app solo en `127.0.0.1:APP_PORT` y túnel opcional por `profile` (610), `scripts/ship.sh` (PC de desarrollo: compila, `docker save | gzip | ssh`) y `scripts/deploy.sh` (servidor: `load`/`infra`/`up`/`rollback`/`status`/`ssh-entry` con lista blanca, backup previo, comprobación de salud y rollback automático) (611), revisión de seguridad (612: 1 bloqueante y 7 menores) y su cierre (613: rollback que sobrevive a un corte de SSH, `lock` en `load`/`infra`, topes de tamaño, `memswap_limit`, documentación de que la clave de despliegue equivale a root).
Medido (Docker Desktop, 6 CPU, 8 GB): imagen 425 MB (jar 68 MB), `docker save` comprimido 131 MB, build en frío 115 s con pico de ~1,2 GB en toda la VM, stack en reposo ~330-380 MB (app ~250-295 MiB, db ~55-80 MiB), volumen de Postgres ~47 MB.
No verificado: envío real por SSH y `restrict,command=` en `sshd`, `flock` en Debian, la cadena Cloudflare → cloudflared → Caddy → app (y que Caddy fije `X-Forwarded-Proto: https`), Cloudflare Access con Bypass, el servidor real (x86_64, Docker 29.9.3, 7,7 GiB con ~2,6 GiB disponibles y swap en uso, 51 GB libres).
Pendiente: tarea 607 (puesta en marcha) y la sesión de deploy con el checklist del servidor; pasos manuales en el runbook (clave restringida `authorized_keys`, `APP_PORT`, `.env` con secretos nuevos, alta del hostname). Decisión abierta: dónde van los backups fuera del servidor (el servidor solo sale a Internet por el túnel).

## Fase F8 · Mapa, refresco, diseño conectado y pantallas (hecha y mergeada a `main`; sin push hasta el cierre)

Reparto (2026-11-22): la sesión principal hace lógica y funcionalidad; la sesión "Diseño visual por naciones" es dueña de lo visual (CSS, tokens, assets, `docs/design/**`). Las ramas de F7 (`f7-deploy-ajustes`, `f7-correcciones-621`) y de F8 están mergeadas a `main`.

| # | Rol | Tarea | Estado | Depende de |
|---|---|---|---|---|
| 801-803 | qa / frontend-dev | Mapa interactivo SVG con menú (tests, lógica, componente) | hecha | — |
| 804-805 | qa / frontend-dev | Refresco de respaldo de la partida (intervalo de 15 s con guarda anti-solape + `visibilitychange`/`online`) | hecha (ver nota) | 803 |
| 806-807 | qa / frontend-dev | Con sesión iniciada, `#/login` redirige al menú | hecha | — |
| 808 | frontend-dev | Conectar el diseño: sprites en `Board`, `.game`/`.player`, menú flotante, colocar flota (arrastrar/girar), login sin texto, LOTUS en front, service worker | hecha (queda: flechas para mover el barco al colocarlo) | 810 |
| 809 | backend-dev | Nación `LOTUS` (migración V4 expand-only) | hecha; `reviewer` aprobó | — |
| 810-811 | qa | Tests del diseño y ajuste del test de login | hecha | — |
| 812, 813, 815-817 | frontend-dev / qa | Perfil (`#/profile`), selector de nación en el registro (se guarda con PATCH tras registrarse), `GameMap` con `map.svg` | hecha | 808 |
| 814 | qa | e2e con las etiquetas en español del mapa | hecha | — |
| 818 | diseño | El ranking ya no desborda en 390 px | hecha | — |
| 819 | backend-dev | Probar que una imagen anterior arranca sobre un esquema con V4 (el default `*:future` de Flyway basta; test `FlywayRollbackTest`) | hecha | 809 |
| 620-622 | devops / reviewer | Deploy al servidor: 621 pidió cambios; 622 los corrigió (sin `@sha256` en `db`, uid 10001, `deploy.sh` a `legacy/`, runbook) y quedó aprobada | hecha; falta el `recibir` real | — |

Nota F8: el PLAN anterior decía que 804 se descartaba y que el refresco se limitaba a volver a primer plano o recuperar la conexión (el fallo no se reprodujo en 7 corridas). Las tareas 804/805 seguían como `pendiente` y se ejecutaron: el front ahora también refresca cada 15 s. Es inofensivo (una petición cada 15 s, sin solapes, nada con la partida terminada) pero contradice esa decisión; si se prefiere la versión mínima, basta quitar el `setInterval` de `GameScreen.tsx`.
Verificado por el chat central: 134 tests de front y 116 de backend en verde, build, e2e en verde y revisión visual a 390, 844x390 y 1280 (el ranking desbordaba a 390 y se corrigió con la 818).
Hallazgos cerrados: el rollback de imagen con V4 arranca sin tocar la configuración (819); el "doble montaje del lobby tras el login" es `StrictMode` en desarrollo (no ocurre en producción); `kim_bauer` del seeder no tiene el rol `PLAYER` y por eso da 403 en `/api/players/me` (usar `marco@aaa.com` / `123`).
Sin hacer, por decisión: chat de texto (v2.1), flechas para mover el barco al colocarlo, icono PWA de más resolución, sprite `sunk` en el tablero rival (el rival no expone sus barcos).
