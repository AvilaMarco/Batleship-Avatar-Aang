#!/usr/bin/env bash
# Uso: scripts/restore.sh shipbender-AAAAMMDD-HHMMSS.dump (archivo dentro de BACKUP_DIR). REEMPLAZA los datos actuales.
# Seguridad: valida el dump ANTES de parar app; guarda un dump de lo actual; pg_restore va en UNA transaccion
# (si falla o se interrumpe, la BD original queda intacta) y app se vuelve a arrancar siempre.
set -euo pipefail
export MSYS_NO_PATHCONV=1   # Git Bash convierte "/backups/..." en rutas de Windows al llamar a docker.exe
cd "$(dirname "$0")/.."
file="${1:?uso: scripts/restore.sh <archivo.dump>}"
case "$file" in */*|*\*) echo "error: pasa solo el nombre del archivo (dentro de BACKUP_DIR)"; exit 1;; esac
bk() { docker compose exec -T "$@" </dev/null; }   # sin stdin: si no, docker se come la respuesta de read
bk backup test -f "/backups/$file" || { echo "error: $file no existe en BACKUP_DIR. No se toco nada."; exit 1; }
bk backup pg_restore -l "/backups/$file" >/dev/null || { echo "error: $file esta corrupto o no es un dump. No se toco nada."; exit 1; }
read -r -p "Se reemplaza la BD por $file. Escribe 'si' para seguir: " ok
[ "$ok" = "si" ] || { echo "cancelado"; exit 1; }
docker compose stop app
trap 'docker compose start app >/dev/null; echo "app arrancada de nuevo"' EXIT   # la BD esta restaurada o intacta, nunca a medias
# RETENTION_DAYS=36500: el dump de seguridad no debe borrar por antiguedad el archivo que vamos a restaurar
out=$(bk -e RETENTION_DAYS=36500 backup sh /scripts/backup.sh) || { echo "error: fallo el dump de seguridad. BD intacta."; exit 1; }
safety="${out##*/}"
echo "dump de seguridad de la BD actual: $safety"
if bk backup sh -c 'pg_restore --clean --if-exists --no-owner --single-transaction --exit-on-error -d "$PGDATABASE" "/backups/$1"' _ "$file"; then
  echo "restauracion ok desde $file"
else
  echo "ERROR: fallo la restauracion. Transaccion revertida: la BD original sigue intacta (copia extra: $safety)."
  exit 1
fi
