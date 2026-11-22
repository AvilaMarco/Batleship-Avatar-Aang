#!/usr/bin/env bash
# PC DE DESARROLLO (Git Bash/WSL). Uso: scripts/ship.sh [--dry-run] [--force]
# Compila la imagen shipbender/app:<sha>, arma el paquete del contrato del servidor (tar sin comprimir con
# servidor.env, compose.yaml e imagenes.tar.gz) y lo envia a `recibir` por SSH. --dry-run: arma y valida, no envia.
# --force: salta "arbol limpio y rama". Sale con el codigo de recibir (0 ok, 1 revertido, 2 rechazado, 3 pendiente de aprobacion, 4 espera cortada).
set -euo pipefail
export MSYS_NO_PATHCONV=1
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."

dry=0; force=0
for a in "$@"; do
  case "$a" in
    --dry-run) dry=1 ;;
    --force) force=1 ;;
    *) echo "uso: scripts/ship.sh [--dry-run] [--force]" >&2; exit 1 ;;
  esac
done

APP=shipbender
MAX_PAQUETE=$((1024 * 1024 * 1024))   # PAQUETE_MAX del servidor: 1 GiB
[ -f .ship.env ] || { echo "error: falta .ship.env (copia .ship.env.example)" >&2; exit 1; }
# shellcheck source=/dev/null
. ./.ship.env
re_name='^[A-Za-z0-9][A-Za-z0-9._-]*$'
[[ "${SHIP_HOST:-}" =~ $re_name ]] || { echo "error: SHIP_HOST invalido en .ship.env (alias ssh, p. ej. tva-deploy)" >&2; exit 1; }
branch="${SHIP_BRANCH:-main}"

if [ "$force" = 0 ]; then
  [ "$(git rev-parse --abbrev-ref HEAD)" = "$branch" ] || { echo "error: hay que estar en $branch (o --force)" >&2; exit 1; }
  [ -z "$(git status --porcelain)" ] || { echo "error: arbol de trabajo sucio (commit o --force)" >&2; exit 1; }
else
  echo "AVISO: --force: la imagen puede no corresponder al commit del TAG" >&2
fi

TAG=$(git rev-parse --short HEAD)
[[ "$TAG" =~ ^[a-z0-9][a-z0-9._-]{0,63}$ && ${#TAG} -ge 7 ]] || { echo "error: version inesperada: $TAG" >&2; exit 1; }

tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
echo "== compilando $APP/app:$TAG (linux/amd64)"
docker build --platform linux/amd64 -t "$APP/app:$TAG" .
docker save "$APP/app:$TAG" | gzip > "$tmp/imagenes.tar.gz"
cp deploy/servidor.env "$tmp/servidor.env"
cp compose.yaml "$tmp/compose.yaml"
tar -C "$tmp" -cf "$tmp/paquete.tar" servidor.env compose.yaml imagenes.tar.gz

# Espejo local de las reglas de formato de recibir: mismos 3 nombres, sin carpetas ni enlaces, <= 1 GiB, APP coincide.
[ "$(tar -tf "$tmp/paquete.tar" | sort | tr '\n' ' ')" = "compose.yaml imagenes.tar.gz servidor.env " ] || { echo "error: contenido del paquete inesperado" >&2; exit 1; }
tar -tvf "$tmp/paquete.tar" | awk '$1 !~ /^-/ { bad=1 } END { exit bad }' || { echo "error: el paquete tiene entradas que no son archivos" >&2; exit 1; }
size=$(wc -c < "$tmp/paquete.tar"); [ "$size" -le "$MAX_PAQUETE" ] || { echo "error: paquete de $size bytes > 1 GiB" >&2; exit 1; }
grep -qx "APP=$APP" "$tmp/servidor.env" || { echo "error: APP del manifiesto != $APP" >&2; exit 1; }
tar -xOf "$tmp/paquete.tar" imagenes.tar.gz | gzip -dc | tar -xOf - manifest.json | grep -q "\"$APP/app:$TAG\"" || { echo "error: la imagen del paquete no se llama $APP/app:$TAG" >&2; exit 1; }
echo "== paquete ok: $size bytes"

if [ "$dry" = 1 ]; then echo "dry-run: no se envia nada"; exit 0; fi
echo "== enviando a $SHIP_HOST (recibir $APP $TAG)"
rc=0
ssh -T -o BatchMode=yes -- "$SHIP_HOST" recibir "$APP" "$TAG" < "$tmp/paquete.tar" || rc=$?
case "$rc" in
  0) echo "desplegado y sano" ;;
  1) echo "fallo: el servidor volvio a la version anterior" >&2 ;;
  2) echo "paquete rechazado (formato/validacion)" >&2 ;;
  3) echo "pendiente de aprobacion del admin: ssh $SHIP_HOST estado $APP" >&2 ;;
  4) echo "se corto la espera del log; el deploy sigue: ssh $SHIP_HOST estado $APP" >&2 ;;
  *) echo "error ssh/recibir (codigo $rc)" >&2 ;;
esac
exit "$rc"
