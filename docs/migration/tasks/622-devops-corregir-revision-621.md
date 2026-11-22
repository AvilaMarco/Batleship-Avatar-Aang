# 622 · devops · Corregir los hallazgos de la revisión 621
Estado: pendiente
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
