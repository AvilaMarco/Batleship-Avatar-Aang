# 608 · devops · Endurecimiento tras la revisión 606
Estado: hecha
Fase: F6
Depende de: 605 (el runbook debe estar terminado: esta tarea también lo edita), 606
Paralelizable: no (toca el runbook)
Origen: hallazgos MENOR 2, 3, 5, 7 y 8 de la tarea 606.
Riesgo: normal

Objetivo: cerrar los menores baratos de la revisión antes de la puesta en marcha.

Archivos: `scripts/backup.sh`, `scripts/restore.sh`, `.env.example`, `.dockerignore`, `docs/deploy/runbook.md`.

Diseño (cerrado):
- `backup.sh`: `umask 077` al principio (los dumps contienen hashes de contraseña).
- `restore.sh` (hallazgo de la 605): imprime "app arrancada de nuevo" aunque `app` no exista (máquina nueva) porque oculta la salida de `start`. Que el mensaje solo diga que arrancó si `docker compose start app` terminó con éxito y avise claramente si `app` no existe (`docker compose up -d app`). Probarlo en el proyecto aislado con `app` sin crear y con `app` existente.
- `.env.example`: `BACKUP_DIR=/ruta/absoluta/en/OTRO/disco/shipbender-backups` como ejemplo (sin ruta real de ninguna máquina) con un comentario "carpeta privada, fuera del repo, otro disco; no una carpeta de nube compartida sin cifrar".
- `.dockerignore`: `**/.env*` además de `.env` y `.env.*`.
- Runbook: secciones breves sobre (a) la carpeta de backups privada (y el riesgo de OneDrive/nube sin cifrar), (b) cómo saber si el backup falla (`docker compose logs backup`) y probar una restauración cada cierto tiempo, (c) rotar el token del túnel en Zero Trust (Tunnels → Refresh token) y `.env` legible solo por tu usuario, (d) no arrancar contenedores sueltos con `docker run` sin `SPRING_PROFILES_ACTIVE=prod` (el perfil por defecto es dev), (e) la regla de rate limiting de Cloudflare es obligatoria.

Criterios de aceptación:
- `git ls-files --eol scripts/` → LF; `bash -n scripts/backup.sh`.
- Con el proyecto aislado `shipbender-test` (`MSYS_NO_PATHCONV=1`, `COMPOSE_PROJECT_NAME=shipbender-test`, `.env` de prueba no versionado, `BACKUP_DIR` temporal fuera del repo): tras un `backup.sh` el dump tiene permisos `-rw-------` (`docker compose exec backup ls -l /backups`).
- `grep -iE 'secret|token|password' docs/deploy/runbook.md` solo nombres de variables.
- Limpieza como en 603 (`down -v` solo del proyecto de prueba, borrar `.env` de prueba y carpeta temporal).
- Commit `build: endurecimiento tras la revisión de seguridad de F6 (tarea 608)`.

## Resultado

- `backup.sh`: `umask 077`; dump verificado con `-rw-------` en `/backups`.
- `restore.sh`: el trap solo dice "app arrancada de nuevo" si `app` existe (`ps -aq`) y `start` tuvo exito; si no, avisa de `docker compose up -d app`. Hallazgo: `docker compose start app` devuelve 0 aunque `app` no exista, por eso hace falta el `ps -aq`. Probado en `shipbender-test`: app inexistente -> AVISO; app existente (imagen dummy via override fuera del repo) -> arrancada y `running`. Logica de transaccion/trap/dump de seguridad intacta.
- `.env.example` (BACKUP_DIR de ejemplo + comentario), `.dockerignore` (`**/.env*`), runbook (secciones a-e; rate limiting pasa de opcional a obligatorio; aviso de la maquina nueva actualizado). `grep` del runbook: solo nombres de variables.
- Limpieza hecha: `down -v` del proyecto de prueba, `.env`, override y carpeta temporal borrados.
