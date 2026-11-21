#!/bin/sh
# Un dump de la BD en /backups y borra los de más de RETENTION_DAYS días.
set -eu
f="/backups/shipbender-$(date +%Y%m%d-%H%M%S).dump"
trap 'rm -f "$f.tmp"' EXIT   # un pg_dump fallido no deja basura en BACKUP_DIR
pg_dump -Fc -f "$f.tmp"
mv "$f.tmp" "$f"   # el .tmp evita dejar un dump a medias con nombre válido
find /backups -name 'shipbender-*.dump' -mtime +"${RETENTION_DAYS:-14}" -delete
echo "backup ok: $f"
