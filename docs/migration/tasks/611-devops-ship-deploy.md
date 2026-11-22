# 611 · devops · Envío de la imagen compilada y despliegue en el servidor (ship.sh + deploy.sh)
Estado: parcial
Fase: F7
Depende de: 610
Paralelizable: no
Origen: usuario, 2026-11-22: compilar en la PC de desarrollo y enviar lo compilado; el servidor (Debian 13, x86_64, Docker 29.9.3, Compose v5.7.1, 7,7 GiB de RAM con ~2,6 GiB disponibles y swap en uso, 51 GB libres) NO debe compilar y solo tiene salida a Internet por el túnel: no puede hacer `docker pull`/`docker build` contra Docker Hub ni registros. Acceso por SSH con clave restringida.
Riesgo: alto (despliegue, acceso SSH y datos)

Objetivo: un comando en la PC de desarrollo compila la imagen, la envía por SSH y el servidor la carga, hace backup, levanta, comprueba la salud y vuelve a la versión anterior si falla.

Archivos: `scripts/ship.sh` (PC de desarrollo, bash: Git Bash/WSL), `scripts/deploy.sh` (servidor), `docs/deploy/runbook.md` (sección "Desplegar una versión"), `.gitignore` (`.ship.env`), `.ship.env.example`.

Diseño (cerrado):
- `ship.sh`: lee `.ship.env` (no versionado: `SHIP_HOST`, `SHIP_USER`, `SHIP_DIR`, `SHIP_KEY`; sin secretos en el repo); exige árbol limpio y rama `main` (con `--force` para saltarlo); `TAG=$(git rev-parse --short HEAD)`; `docker build --platform linux/amd64 -t shipbender:$TAG .`; envía en streaming `docker save shipbender:$TAG | gzip | ssh ... "deploy.sh load $TAG"` (sin fichero temporal; desde Git Bash, no PowerShell); con `--with-db` incluye también `postgres:17-alpine` en el mismo stream (la primera vez, o si el servidor no lo tiene: el servidor no puede hacer pull); envía `docker-compose.yml` y `scripts/` (por el mismo canal, `tar`) para que el servidor tenga la infraestructura exacta de ese commit; luego `deploy.sh up $TAG`.
- `deploy.sh load|up|rollback|status` en el servidor (`set -euo pipefail`, comillas en todo, sin interpolar argumentos en comandos): `load` valida el TAG (`^[0-9a-f]{7,12}$`) y hace `gunzip | docker load`; `up`: (1) `backup.sh` si ya hay una versión corriendo (la migración de Flyway no se deshace con un rollback: el dump previo es la salvaguarda), (2) guarda el TAG actual en `.current_tag` y el anterior en `.previous_tag`, (3) `APP_TAG=$TAG docker compose up -d --no-build db app backup`, (4) espera `healthy` de `app` (máx. 180 s); si no llega, vuelve automáticamente a `.previous_tag` y sale con error claro; `rollback` vuelve al anterior; `status` muestra versión, salud y últimos logs.
- Acceso SSH: documentar la clave restringida en `authorized_keys` del servidor: `restrict,command="/ruta/deploy.sh ssh-entry"` donde `ssh-entry` lee `$SSH_ORIGINAL_COMMAND` y solo acepta `load <tag>`, `up <tag>`, `rollback`, `status` y la recepción del `tar` de infraestructura (lista blanca estricta; todo lo demás rechazado). El usuario instala la clave; el central nunca ve claves privadas.
- `docker-compose.yml` ya no construye en el servidor: `up` usa `--no-build` y la imagen cargada (`shipbender:$TAG`).

Criterios de aceptación: no hay servidor SSH disponible en esta máquina; verificar lo verificable y declarar lo demás:
- `bash -n` de ambos scripts; `shellcheck` si está instalado; LF (`git ls-files --eol`).
- Prueba local de `deploy.sh` contra el Docker de esta máquina (proyecto aislado `COMPOSE_PROJECT_NAME=shipbender-test`, `MSYS_NO_PATHCONV=1`, `.env` de prueba no versionado con APP_PORT=4756, BACKUP_DIR temporal fuera del repo): `load` con `docker save | gunzip`-equivalente local, `up` con dos TAGs distintos (construye dos imágenes de prueba con etiquetas distintas), comprobar `.current_tag`/`.previous_tag`, `rollback`, y un `up` con una imagen que NO llega a healthy (por ejemplo, con `JWT_SECRET` demasiado corto) → vuelve solo al anterior y sale con error.
- Entradas hostiles al `ssh-entry`: `load ../x`, `up $(id)`, `load abc; rm -rf /`, comando vacío → todas rechazadas sin ejecutar nada (probar con `SSH_ORIGINAL_COMMAND` simulado).
- NO verificado y a declarar en el Resultado: el envío real por SSH y la clave restringida (no hay servidor aquí); `docker save | ssh | docker load` solo se prueba contra un canal local.
- Limpieza como en 603.
- Commit `build: ship.sh y deploy.sh para enviar la imagen compilada y desplegar con rollback (tarea 611)`.

## Resultado

Hecho: `scripts/deploy.sh` (servidor: `load|infra|up|rollback|status|ssh-entry`), `scripts/ship.sh` (PC de desarrollo), `.ship.env.example`, `.gitignore` (`.ship.env`, `.current_tag`, `.previous_tag`, `.deploy.lock`, `*.new`), runbook secciones 3 y 4 (+ clave restringida).

Verificado (Docker local, `COMPOSE_PROJECT_NAME=shipbender-test`, APP_PORT=4756, BACKUP_DIR temporal, `.env`/`.ship.env` de prueba ya borrados):
- `bash -n` ok; LF ok; shellcheck NO instalado (no ejecutado).
- `ship.sh --with-db` completo (build + save|gzip|ssh|load + tar `infra` + `up`) con un `ssh` falso que ejecuta `deploy.sh ssh-entry` con `SSH_ORIGINAL_COMMAND`: despliega healthy, carga también `postgres:17-alpine`.
- `up` con dos TAGs: `.current_tag`/`.previous_tag` correctos; backup previo con `backup.sh` (dump nuevo en cada `up`/`rollback`); `rollback` manual y rollback doble (swap) ok.
- Rollback AUTOMATICO: imagen en crashloop (falla en ~20 s por reinicios) e imagen colgada (`DEPLOY_WAIT=20`): vuelve sola a la anterior, estado restaurado, `rc=1`, mensaje y logs.
- 24 entradas hostiles a `ssh-entry` (`load ../x`, `up $(id)`, `load abc; rm -rf /`, vacío, sin variable, saltos de línea, mayúsculas, espacios, backticks, `|`, `&&`...): todas `rc=126` y el archivo canario no se creó. `infra` rechaza hardlinks, rutas ajenas, nombres ocultos y basura; `load` rechaza no-gzip; `up` con imagen inexistente no toca nada.
- Hallazgo (causa raiz): con `JWT_SECRET` corto en `.env` el `up` falla y el rollback automático TAMBIEN falla (mismo `.env`), `rc=1` con "CRITICO"; es una limitación inherente: el rollback solo cubre fallos de la imagen, no de la configuración (documentado en el runbook). Bug propio hallado y corregido en las pruebas: `trap EXIT` con variable `local` (unbound variable) dejaba temporales.

NO verificado: envío real por SSH, `authorized_keys` con `restrict,command=` y el comportamiento de sshd con la clave; la ruta `flock` (Git Bash no lo trae; en Debian si, sin probar); `ssh-entry` bajo `sh` no-bash no aplica (shebang bash); rechazo de symlinks en `infra` (no se pudo crear uno en Windows; el mismo filtro de tipo rechazó un hardlink); `docker save|ssh` solo contra canal local; tamaño/tiempo de transferencia real.

Decisiones/desviaciones: se omite `SHIP_DIR` (el comando forzado fija el directorio en el servidor; el cliente no lo elige) y se añade `SHIP_BRANCH` (por defecto `main`). `deploy.sh` ignora SIGHUP para terminar un despliegue aunque se corte el ssh.
