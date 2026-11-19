# The Last Shipbender

Batalla naval online 1 vs 1 por turnos, temática Avatar. Chat/emotes en tiempo real.
Proyecto en migración: ver `docs/migration/PLAN.md` (estado de todo) antes de empezar.

## Stack actual → objetivo
- Backend: Spring Boot 2.6 / Java 17 → Spring Boot 4.x / Java 21
- Front: JS vanilla en `src/main/resources/static/web` → React + TS + Vite en `/frontend`
- BD: H2 en memoria → Postgres + Flyway (no hay datos que conservar)
- Tiempo real: STOMP sobre WebSocket (`/the-last-shipbender`, prefijos `/app` y `/topic`)
- Deploy: Docker Compose (app + Postgres + cloudflared) en servidor local con Cloudflare Tunnel

## Comandos
- Tests: `./mvnw test`
- Levantar: `./mvnw spring-boot:run` → http://localhost:8080/web/games.html · Swagger en `/swagger`

## Glosario del dominio
- **Player**: usuario registrado (rol GUEST / PLAYER / ADMIN), elige una **Nation** (agua, tierra, fuego, aire).
- **Game**: partida; estados `CREATED` → `IN_GAME` → `FINISH`. Tiene una ubicación en el mapa.
- **GamePlayer**: participación de un Player en un Game (une ambos). Tiene ships y salvos.
- **Ship**: barco colocado en la grilla (tipo, celdas). **Salvo**: ronda de disparos de un turno.
- **Emote**: reacción enviada en tiempo real al rival.

## Estructura backend (`com.codeblockacademy.shipbender`)
config · controller · dto · enums · exception · models · repository · security · service (+ intereface, model, validations)

## Reglas de trabajo con IA
- Roles en `.claude/agents/`. Cada rol ejecuta solo tareas con su nombre en `docs/migration/tasks/`.
- Si necesitas algo de otro rol: crea `docs/migration/tasks/NNN-<rol>-<tema>.md` (estado `pendiente`), no lo hagas tú.
- `PLAN.md` y `docs/learning/APRENDIZAJE.md` los edita solo el chat central.
- Lee solo lo necesario: este archivo, tu tarea y los archivos que ella nombra.
- Al terminar: escribe la sección `## Resultado` de la tarea y devuelve ≤10 líneas + notas de aprendizaje
  (antes / problema / después / por qué es mejor / patrón o principio).
- Commits: Conventional Commits (`docs/conventional_commits.md`), uno por tarea, en la rama de la fase.
- Ramas: `main` es la principal; cada fase en `migration/fN-<nombre>`; merge a `main` solo con aprobación del usuario.
- Nunca subir secretos: van en `.env` (ignorado por git).
