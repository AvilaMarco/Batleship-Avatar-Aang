# 622 · devops · Corregir los hallazgos de la revisión 621
Estado: parcial
Fase: F7
Depende de: 621
Paralelizable: sí (solo deploy/docs; sin frontend ni backend)
Origen: tarea 621 (BLOQUEANTE 1 y MENORES 2-6); usuario: "procede con todas las tareas pendientes y no preguntes".
Riesgo: alto (deploy) → revisión rápida del reviewer tras el cambio.

Objetivo y decisiones (cerradas):
1. BLOQUEANTE: en `compose.yaml` quitar el `@sha256:…` de la imagen `postgres:17.11-alpine` (el tag exacto sigue fijo, sin `latest`); una imagen cargada con `docker load` no conserva el digest. Documentar en `PRIMER-DESPLIEGUE.md`/checklist que el servidor debe tener `postgres:17.11-alpine` precargada (o hacer `docker pull` una vez); no ampliar el formato de `recibir`.
2. `scripts/deploy.sh`: mover a `docs/deploy/legacy/` con cabecera "OBSOLETO, solo referencia" (sin permiso de ejecución) y quitar su mención como entregable en `RESPUESTA-AL-SERVIDOR-2.md`.
3. Reescribir `docs/deploy/runbook.md` y `docs/deploy/CHECKLIST-SERVIDOR.md` al flujo vigente (`ship.sh` → `recibir`, `compose.yaml`).
4. Rollback: documentar en el runbook que el rollback solo cambia la imagen; con migraciones nuevas (V4 LOTUS) antes de volver atrás hacer `update player set nation=null where nation='LOTUS'` (y en `game`) o restaurar el backup; migraciones expand/contract.
5. Añadir a `PRIMER-DESPLIEGUE.md` y al checklist: `data/pgdata` con dueño 70 y modo 0700.
6. Alinear el usuario de la app: `adduser -u 10001` (y grupo) en el `Dockerfile` para que coincida con `user: 10001:10001`.

Criterios de aceptación:
- `docker compose -f compose.yaml config` (con las variables de `deploy/servidor.env` de ejemplo) → exit 0 y sin `@sha256` en `db`.
- `docker build .` → exit 0 y `docker run --rm --entrypoint id <img>` muestra uid 10001 (si Docker no puede, marcar parcial).
- `grep -rn "deploy.sh" docs scripts compose.yaml` → solo referencias a `docs/deploy/legacy/`.
- Commits Conventional Commits, uno por grupo, terminados con la línea Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado
Hechos (3 commits: `fix(deploy)`, `chore(deploy)`, `docs(deploy)`):
1. `compose.yaml`: `db` usa `postgres:17.11-alpine` sin `@sha256`. Documentado en PRIMER-DESPLIEGUE (pre-requisitos) y en RESPUESTA-AL-SERVIDOR-2 (pregunta 2).
2. `deploy.sh` movido a `docs/deploy/legacy/deploy.sh` con cabecera OBSOLETO, modo 100644 en el índice. Retirado como entregable en RESPUESTA-AL-SERVIDOR-2 y RESPUESTA-AL-SERVIDOR.
3. `runbook.md` y `CHECKLIST-SERVIDOR.md` reescritos al flujo `ship.sh` → `recibir` (códigos 0-4, sin `ssh-entry`, sin servicio backup).
4. Rollback documentado: solo cambia la imagen; V4 LOTUS exige `update ... set nation=null where nation='LOTUS'` en `player` y `game` antes de volver a una versión anterior a V4, o restaurar el backup.
5. `data/pgdata` 70:70 modo 0700 en PRIMER-DESPLIEGUE (pre-requisitos) y checklist.
6. `Dockerfile`: `addgroup -S -g 10001 app && adduser -S -u 10001 -G app app`.

Verificado:
- `docker build .` exit 0; `docker run --rm --entrypoint id shipbender/app:622test` → uid=10001(app) gid=10001.
- `docker compose -p cfgcheck -f compose.yaml -f deploy/compose.local.yaml --env-file deploy/servidor.env config` exit 0 (con VERSION, JWT_SECRET, POSTGRES_PASSWORD de ejemplo); 0 apariciones de `sha256`.

Pendiente (por eso `parcial`):
- Criterio `docker compose -f compose.yaml config` sin overlay: falla con "service db refers to undefined network interna". Es el diseño del contrato paquete (las redes las añade el servidor); el overlay `deploy/compose.local.yaml` las define. Decidir si el criterio se cambia a usar el overlay.
- `grep -rn "deploy.sh" docs scripts compose.yaml` no da solo `docs/deploy/legacy/`: quedan menciones en registros históricos (`docs/migration/tasks/607,611,612,613,620,621`), en `docs/migration/PLAN.md` y `docs/learning/APRENDIZAJE.md` (solo los edita el chat central). No se reescriben registros históricos.
- Atribución: el criterio pedía `Co-Authored-By: Claude Sonnet 5.5`; los commits llevan `Claude Haiku 5.5` por la instrucción de atribución del sistema.
- Sin probar `recibir` ni el runner (no están en el repo); el runbook describe el flujo según ADR 007 y PRIMER-DESPLIEGUE.
- `frontend/`, `src/`, puertos 4703-4705 no tocados.
