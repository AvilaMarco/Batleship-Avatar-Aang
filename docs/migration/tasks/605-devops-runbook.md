# 605 · devops · Runbook de operación
Estado: hecha
Fase: F6
Depende de: 604
Paralelizable: sí, con 606 (solo escribe un doc)
Origen: ADR 007 punto 13; pedido del usuario "un runbook de operación (actualizar versión, rollback, ver logs, restaurar)".
Riesgo: normal

Objetivo: una persona puede actualizar, hacer rollback, ver logs, hacer un backup y restaurar sin leer el ADR.

Archivos: `docs/deploy/runbook.md` (excepción explícita a tu alcance: solo este archivo).

Diseño (cerrado): el ADR 007 Anexo E, más la primera instalación (ADR 007 "Pasos manuales" 2, 7 y 8) y una sección "Problemas conocidos": WebSocket 403 → revisar `X-Forwarded-Proto`/Host (ADR 007 punto 6, plan B); `MSYS_NO_PATHCONV`; después de un reinicio el juego no vuelve hasta iniciar sesión (U7). Cada comando, probado contra el stack de prueba de 603/604 (`COMPOSE_PROJECT_NAME=shipbender-test`).

Criterios de aceptación:
- Cada comando del runbook se ejecutó una vez contra el proyecto de prueba (anotarlo en el Resultado), incluido un ciclo actualizar → rollback con dos `APP_TAG` distintos.
- El runbook no contiene ningún secreto ni ningún token (revisar con `grep -iE 'secret|token|password' docs/deploy/runbook.md`: solo nombres de variables).
- Advertencia visible sobre `down -v` / `volume prune`.
- Commit `docs: runbook de operación del deploy (tarea 605)`.

## Resultado
`docs/deploy/runbook.md` con: advertencia `down -v`, sección de SO (pendiente de confirmar el SO), primera instalación, estado/logs, actualizar, rollback, backups/restauración, rotar secretos, parar/arrancar, problemas conocidos. Hostname `shipbender.racnar.dev`, JWT 480 min, `BACKUP_DIR` fuera del repo.

Probado contra `COMPOSE_PROJECT_NAME=shipbender-test` (`.env` de prueba, `BACKUP_DIR` temporal fuera del repo; todo borrado al final):
- Probados: `docker compose config -q`, `up -d --build` (db/app/backup y luego completo), `ps`, `logs` (con `-f`, `--tail`, varios servicios), `exec app wget` (home), `docker image ls shipbender`, ciclo actualizar (backup previo + `sed` de `APP_TAG` con `git rev-parse --short HEAD` + `up -d --build app`) -> rollback (`APP_TAG=test1`, `up -d app`) con dos tags distintos, `backup.sh` a mano, `exec backup ls`, `restore.sh` (BD 2 jugadores -> 1; y con app no creada, caso "máquina nueva"), rotar `JWT_SECRET` y `TUNNEL_TOKEN` (`up -d app` / `up -d tunnel`; el túnel falla con token falso, como se documenta), `down` + `up -d db backup` conservando datos, `openssl rand -base64 48`.
- No probados: `git clone`/`git pull`, `cp .env.example .env` (trivial), paso a paso de Cloudflare, conexión real del túnel y WebSocket 403 (es la tarea 607), reinicio del servidor, Linux/systemd, rollback con migración Flyway nueva.
- Hallazgo: en Git Bash un `BACKUP_DIR=/tmp/...` crea el bind en la VM de Docker Desktop y el dump no aparece en el host; hay que usar ruta de Windows (`C:/...`). Documentado.
- Hallazgo menor (script, no tocado): `restore.sh` imprime "app arrancada de nuevo" aunque `app` no exista (máquina nueva); documentado en el runbook.
- grep `secret|token|password`: solo nombres de variables y texto.

