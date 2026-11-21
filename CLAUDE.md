# The Last Shipbender

Batalla naval online 1 vs 1 por turnos, temática Avatar. Chat/emotes en tiempo real.
Proyecto en migración: ver `docs/migration/PLAN.md` (estado de todo) antes de empezar.

## Stack actual → objetivo
- Backend: Spring Boot 4.2 / Java 21 (migrado en F3)
- Front: React + TypeScript + Vite en `/frontend` (migrado en F5; se empaqueta en el jar con `-Pfrontend`)
- BD: Postgres 17 + Flyway (`db/migration`); H2 eliminado
- Tiempo real: STOMP sobre WebSocket (`/the-last-shipbender`, prefijos `/app` y `/topic`)
- Deploy: Docker Compose (app + Postgres + cloudflared) en servidor local con Cloudflare Tunnel

## Comandos
- Tests: `./mvnw test` (requiere Docker corriendo: Testcontainers)
- Backend (requiere Docker: Postgres en contenedor): `PORT=4704 ./mvnw spring-boot:test-run` · Swagger en `/swagger`
- Front: `cd frontend && npm run dev` (Vite en 4705, proxy al backend 4704) · `npm test` (Vitest) · `npm run e2e` (Playwright; backend en 4704)
- Jar con front dentro: `./mvnw -Pfrontend -DskipTests package` (descarga Node 22; `./mvnw test` no lo necesita)

## Glosario del dominio
- **Player**: usuario registrado (rol GUEST / PLAYER / ADMIN), elige una **Nation** (agua, tierra, fuego, aire).
- **Game**: partida; estado derivado `CREATED` → `IN_GAME` → `FINISH`. Ubicación en el mapa, grilla 10×10, turnos simultáneos de 5 disparos.
- **GamePlayer**: participación de un Player en un Game (une ambos). Tiene ships y salvos.
- **Ship**: barco (flota 5-4-3-3-2). **Salvo**: los 5 disparos de un jugador en un turno.
- **Emote**: reacción enviada en tiempo real al rival.

## Estructura backend (`dev.racnar.shipbender`, por feature)
- `player` (registro, perfil, ranking), `game` (partidas, barcos, salvos; `domain/` = reglas en Java puro, `event/` = eventos STOMP), `chat` (emotes), `security` (JWT, STOMP), `config`, `shared/error` (`ShipbenderException` + `ErrorCode`, `ProblemDetail`).
- Contrato: `docs/adr/004-contrato-api-v2.md` (REST + STOMP). Arquitectura: `docs/adr/005-arquitectura-backend.md`.

## Estructura front (`frontend/src`)
- `domain/` reglas del juego en TS puro (sin React/red) · `api/` cliente REST+STOMP tipado y único acceso al token (`auth.ts`) · `auth/` contexto de sesión · `screens/` login, lobby, partida · `components/`. Rutas por hash (`#/login`, `#/`, `#/games/{id}`). Decisiones: `docs/adr/006-frontend-react.md`.

## Flujo por petición (chat central · metodología `proyecto-ia` v3)
- Preguntas/revisiones: solo lectura. Premisa de un bug: verificarla antes de arreglar.
- Pequeño (se retoma con la petición + `git diff`): en línea, sin tarea. Grande: tareas en `docs/migration/tasks/`. Riesgo alto (seguridad, migraciones Flyway, datos): tarea de `reviewer` antes del merge.
- Test primero. Verificar por cuenta propia lo que reporta cada rol (git log, tests; repetir si hay intermitencia → causa raíz).
- No verificable por el rol (navegador, Docker, permiso denegado) = `parcial`; lo cubre el central o el usuario. Nunca saltar permisos.
- Paralelizar solo tareas sin archivos ni build/puertos/Docker en común.
- Cierre de fase: aprendizajes explicados + aprobación del usuario antes del merge (skill `proyecto-ia`).

## Reglas de trabajo con IA
- Tareas: campos `Origen` (frase literal del usuario, tarea o ADR) y `Riesgo`; diff ≤~400 líneas o se parte.
- Tests: los devs añaden tests de su código nuevo (TDD); solo `qa` modifica o borra tests existentes.
- Roles en `.claude/agents/` (`reviewer` es global, solo lee). Cada rol ejecuta solo tareas con su nombre en `docs/migration/tasks/`.
- Si necesitas algo de otro rol: crea `docs/migration/tasks/NNN-<rol>-<tema>.md` (estado `pendiente`), no lo hagas tú.
- `PLAN.md` y `docs/learning/APRENDIZAJE.md` los edita solo el chat central.
- Lee solo lo necesario: este archivo, tu tarea y los archivos que ella nombra.
- Al terminar: escribe la sección `## Resultado` de la tarea y devuelve ≤10 líneas + notas de aprendizaje
  (antes / problema / después / por qué es mejor / patrón o principio).
- Commits: Conventional Commits (`docs/conventional_commits.md`), uno por tarea, en la rama de la fase.
- Ramas: `main` es la principal; cada fase en `migration/fN-<nombre>`; merge a `main` solo con aprobación del usuario.
- Nunca subir secretos: van en `.env` (ignorado por git).

## Puertos de desarrollo
- Principal (rama `main`): 4703
- Pruebas (cualquier otra rama): 4704 o 4705
Reglas:
- Todo script/servidor que necesite puerto usa uno de estos 3 y fija el puerto con `--port <p> --strictPort` (o equivalente); nunca dejar que elija otro.
- En `main` usar siempre 4703. En otra rama usar 4704; si está ocupado por otra sesión de esta app, 4705.
- Si el puerto elegido está ocupado: identificar el proceso (`Get-NetTCPConnection -LocalPort <p> -State Listen`), y si es de esta app (o un servidor de desarrollo huérfano) detenerlo con `Stop-Process -Id <pid>` y reintentar en el mismo puerto. Si es de otra cosa, probar el siguiente de los 3; si ninguno sirve, avisar al usuario.
- Spring Boot: `server.port=${PORT:4703}` (falla si el puerto está ocupado, no salta a otro). En ramas de prueba: `PORT=4704 ./mvnw spring-boot:run`. Postgres/cloudflared dentro de Docker no usan estos puertos.
