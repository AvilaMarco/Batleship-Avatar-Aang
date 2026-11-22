#!/usr/bin/env bash
# PC DE DESARROLLO (Git Bash/WSL). Uso: scripts/ship.sh [--with-db] [--force]
# Compila la imagen aqui, la envia por SSH en streaming (sin ficheros temporales) y el servidor la carga y despliega (deploy.sh).
# --with-db: incluye postgres:17-alpine (la primera vez: el servidor no puede hacer pull). --force: salta "arbol limpio y rama".
set -euo pipefail
export MSYS_NO_PATHCONV=1
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."

with_db=0; force=0
for a in "$@"; do
  case "$a" in
    --with-db) with_db=1 ;;
    --force) force=1 ;;
    *) echo "uso: scripts/ship.sh [--with-db] [--force]" >&2; exit 1 ;;
  esac
done

[ -f .ship.env ] || { echo "error: falta .ship.env (copia .ship.env.example)" >&2; exit 1; }
# shellcheck source=/dev/null
. ./.ship.env
re_name='^[A-Za-z0-9][A-Za-z0-9._-]*$'
[[ "${SHIP_HOST:-}" =~ $re_name && "${SHIP_USER:-}" =~ $re_name ]] || { echo "error: SHIP_HOST/SHIP_USER invalidos en .ship.env" >&2; exit 1; }
[ -r "${SHIP_KEY:-}" ] || { echo "error: SHIP_KEY no apunta a una clave legible" >&2; exit 1; }
branch="${SHIP_BRANCH:-main}"

if [ "$force" = 0 ]; then
  [ "$(git rev-parse --abbrev-ref HEAD)" = "$branch" ] || { echo "error: hay que estar en $branch (o --force)" >&2; exit 1; }
  [ -z "$(git status --porcelain)" ] || { echo "error: arbol de trabajo sucio (commit o --force)" >&2; exit 1; }
else
  echo "AVISO: --force: la imagen puede no corresponder al commit del TAG" >&2
fi

TAG=$(git rev-parse --short HEAD)
[[ "$TAG" =~ ^[0-9a-f]{7,12}$ ]] || { echo "error: TAG inesperado: $TAG" >&2; exit 1; }

# El comando remoto es siempre una cadena fija + TAG validado; el servidor lo vuelve a validar (ssh-entry).
rsh() { ssh -T -i "$SHIP_KEY" -o BatchMode=yes -o IdentitiesOnly=yes -- "$SHIP_USER@$SHIP_HOST" "$1"; }

echo "== compilando shipbender:$TAG (linux/amd64)"
docker build --platform linux/amd64 -t "shipbender:$TAG" .

imgs=("shipbender:$TAG")
if [ "$with_db" = 1 ]; then imgs+=("postgres:17-alpine"); fi   # mantener igual que la imagen de db en docker-compose.yml
echo "== enviando ${imgs[*]}"
docker save "${imgs[@]}" | gzip | rsh "load $TAG"

echo "== enviando docker-compose.yml y scripts/"
tar -cf - docker-compose.yml scripts | rsh "infra"

echo "== desplegando $TAG"
rsh "up $TAG"
