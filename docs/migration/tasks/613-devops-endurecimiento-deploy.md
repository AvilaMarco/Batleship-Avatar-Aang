# 613 · devops · Endurecimiento del despliegue tras la revisión 612
Estado: pendiente
Fase: F7
Depende de: 612
Paralelizable: no
Origen: hallazgos BLOQUEANTE 1 y MENOR 2-8 de la tarea 612.
Riesgo: alto (punto de entrada SSH y rollback)

Objetivo: que un corte de SSH a mitad de un despliegue no deje el servidor en la versión mala, y cerrar los menores baratos.

Archivos: `scripts/deploy.sh`, `scripts/backup.sh`, `docker-compose.yml`, `.gitignore`, `docs/deploy/runbook.md`.

Diseño (cerrado):
1. BLOQUEANTE: al inicio de `cmd_up` y `cmd_rollback`, desacoplar la salida de la sesión SSH: `exec >>"$DIR/.deploy.log" 2>&1` (con `tee` solo si hay terminal) y `trap '' HUP PIPE`. `.deploy.log` al `.gitignore` (permisos 0600). Documentar en el runbook: tras un corte, consultar `deploy.sh status` y `.deploy.log`.
2. Tomar `lock` también en `cmd_load` y `cmd_infra`.
3. `infra`: rechazar si el número de líneas de `tar -tf` difiere del de entradas de `tar -tvf` (nombres con salto de línea); y un tope de tamaño (p. ej. 8 MiB para `infra`) leyendo con `head -c` y fallando si se excede. `load`: tope razonable (p. ej. 2 GiB) con `head -c`.
4. `backup.sh`: pasar el archivo a UTF-8 (comentario de la línea 55 corrupto).
5. `docker-compose.yml`: `memswap_limit` igual a `mem_limit` en `db`, `app`, `tunnel` y `backup` (que ningún contenedor use el swap del servidor).
6. Runbook: (a) "la clave de despliegue equivale a root del servidor": clave solo para este fin, `from="IP"` en `authorized_keys`, usuario sin sudo ni shell interactivo; (b) nota sobre el volumen de dumps (uno por despliegue; los dumps son 0600 de root, el usuario de despliegue no los lee).

Criterios de aceptación (proyecto aislado `COMPOSE_PROJECT_NAME=shipbender-test`, `MSYS_NO_PATHCONV=1`, `.env` y `.ship.env` de prueba no versionados, APP_PORT=4756, BACKUP_DIR temporal fuera del repo con ruta C:/...):
- `bash -n`, LF y UTF-8 (`file scripts/*.sh`) correctos.
- Reproducir el bloqueante ANTES del arreglo y demostrar que queda corregido: un `up` con una imagen que no llega a healthy, ejecutado con la salida (stdout/stderr) cerrada a mitad (p. ej. `deploy.sh up TAG | head -n 1` que cierra la tubería) → ANTES: queda la versión mala; DESPUÉS: hace rollback a la anterior y `.deploy.log` lo registra.
- Las 9+ entradas hostiles siguen rechazadas con 126 (archivo canario).
- `infra` rechaza un tar con tamaño excesivo y uno con desajuste de entradas (simúlalo con un tar construido a mano sin saltos de línea si Windows no deja crearlos: al menos prueba el tope de tamaño).
- `docker compose config` muestra `memswap_limit` en los 4 servicios.
- Limpieza como en 603.
- Commit `fix(deploy): rollback ante corte de SSH, lock en load/infra, topes de tamaño y memswap (tarea 613)`.

## Resultado
