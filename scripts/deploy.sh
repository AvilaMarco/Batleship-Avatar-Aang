#!/usr/bin/env bash
# SERVIDOR (Debian). Uso: scripts/deploy.sh load <tag> | infra | up <tag> | rollback | status | ssh-entry
# ssh-entry es el punto de entrada de la clave SSH restringida (authorized_keys: restrict,command="/ruta/scripts/deploy.sh ssh-entry"):
# lee SSH_ORIGINAL_COMMAND y solo acepta una lista blanca; nada del cliente llega a un shell (sin eval, sin sh -c, sin interpolar).
# Seguridad de datos: `up` hace backup antes (Flyway no se deshace) y vuelve solo a la version anterior si la nueva no llega a healthy.
set -euo pipefail
export LC_ALL=C MSYS_NO_PATHCONV=1
trap '' HUP PIPE   # si se cae el ssh a mitad de un despliegue, este termina igual (nunca dejar app sin version)
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."

TAG_RE='^[0-9a-f]{7,12}$'
MAX_LOAD=$((2 * 1024 * 1024 * 1024))   # tope del flujo de `load` (comprimido): llenar el disco no es gratis
MAX_INFRA=$((8 * 1024 * 1024))
WAIT_S="${DEPLOY_WAIT:-180}"   # solo por entorno local: ssh no pasa variables
[[ "$WAIT_S" =~ ^[0-9]{1,4}$ ]] || { echo "error: DEPLOY_WAIT invalido" >&2; exit 1; }

die() { echo "error: $*" >&2; exit 1; }
tag_ok() { [[ "${1:-}" =~ $TAG_RE && "$1" != *$'\n'* ]] || die "TAG invalido (se espera $TAG_RE)"; }
# un .current_tag/.previous_tag manipulado tampoco llega a docker: se revalida al leerlo
read_tag() { local t=""; [ ! -f "$1" ] || t=$(head -n1 "$1" | tr -d '\r'); [ -z "$t" ] || tag_ok "$t"; printf '%s' "$t"; }
save_tags() {   # $1 = actual, $2 = anterior (puede ir vacio); escritura atomica
  printf '%s\n' "$1" > .current_tag.new && mv -f .current_tag.new .current_tag
  printf '%s\n' "$2" > .previous_tag.new && mv -f .previous_tag.new .previous_tag
}
# up/rollback: la salida va a .deploy.log (0600) y de ahi a la sesion. Si el ssh se corta, escribir en la tuberia rota
# no puede matar al script (SIGPIPE/EPIPE) antes del rollback. Tras un corte: scripts/deploy.sh status y .deploy.log.
detach() {
  (umask 077; : >> .deploy.log)
  # warn-nopipe: tee sigue escribiendo el log aunque la tuberia de la sesion este rota (PIPE ignorado, ver trap de arriba)
  exec > >(tee -a --output-error=warn-nopipe .deploy.log) 2>&1
  trap 'exec >&- 2>&-; wait' EXIT   # que tee vuelque las ultimas lineas antes de terminar
  echo "== $(date -u +%FT%TZ) $* =="
}
lock() {   # shortcut: sin flock (p. ej. Git Bash) no hay exclusion; en el servidor Debian si
  command -v flock >/dev/null || return 0
  exec 9>.deploy.lock
  flock -n 9 || die "hay otro despliegue en curso"
}

# Todas las imagenes del compose deben estar ya cargadas: el servidor no puede hacer pull (compose intentaria bajarlas).
check_images() {
  local img
  while IFS= read -r img; do
    docker image inspect "$img" >/dev/null 2>&1 || die "falta la imagen $img (si es postgres: ship.sh --with-db). No se toco nada."
  done < <(docker compose config --images)
}

backup_if_running() {
  if [ -n "$(docker compose ps -q --status running app)" ]; then
    docker compose exec -T backup sh /scripts/backup.sh </dev/null || die "fallo el backup previo. No se toco nada."
  else
    echo "app no esta corriendo: sin backup previo"
  fi
}

# 0 = app healthy; 1 = unhealthy, reinicios en bucle o timeout
wait_healthy() {
  local end=$((SECONDS + WAIT_S)) id st
  while [ "$SECONDS" -lt "$end" ]; do
    id=$(docker compose ps -q app) || id=""
    if [ -n "$id" ]; then
      st=$(docker inspect -f '{{.State.Health.Status}} {{.RestartCount}}' "$id" 2>/dev/null) || st=""
      case "$st" in
        "healthy "*) return 0 ;;
        "unhealthy "*) return 1 ;;
      esac
      [ -z "$st" ] || [ "${st#* }" = 0 ] || return 1   # crashloop: no esperar los 180 s
    fi
    sleep 3
  done
  return 1
}

up_and_wait() { docker compose up -d --no-build db app backup && wait_healthy; }   # APP_TAG exportado por el llamador

cmd_load() {
  tag_ok "${1:-}"
  lock
  # si se pasa del tope, head corta el gzip y gunzip falla (pipefail): no se carga nada a medias
  head -c "$MAX_LOAD" | gunzip | docker load || die "fallo la carga (flujo corrupto o mayor de 2 GiB)"
  docker image inspect "shipbender:$1" >/dev/null 2>&1 || die "el flujo no contenia shipbender:$1"
}

# Recibe un tar (docker-compose.yml + scripts/) y lo valida entero ANTES de extraer: solo ficheros normales con nombre plano.
cmd_infra() {
  local f b n   # d global: el trap EXIT corre cuando la funcion ya termino
  lock
  d=$(mktemp -d "${TMPDIR:-/tmp}/shipbender-infra.XXXXXX")
  trap 'rm -rf "$d"' EXIT
  head -c $((MAX_INFRA + 1)) > "$d/in.tar"
  [ "$(wc -c < "$d/in.tar")" -le "$MAX_INFRA" ] || die "el tar supera 8 MiB"
  tar -tf "$d/in.tar" > "$d/names"
  tar -tvf "$d/in.tar" > "$d/long"
  cut -c1 "$d/long" > "$d/types"
  # un nombre con salto de linea parte una entrada en dos lineas: lineas de nombres, de listado largo y entradas reales deben coincidir
  n=$(wc -l < "$d/names")
  [ "$(wc -l < "$d/long")" = "$n" ] && [ "$(grep -cE '^[-d][-rwxsStT]{9} ' "$d/long")" = "$n" ] || die "el tar trae nombres con saltos de linea"
  grep -qx 'docker-compose.yml' "$d/names" || die "el tar no trae docker-compose.yml"
  ! grep -qvE '^(docker-compose\.yml|scripts/|scripts/[A-Za-z0-9][A-Za-z0-9._-]*)$' "$d/names" || die "el tar trae rutas no permitidas"
  ! grep -qvE '^[-d]$' "$d/types" || die "el tar trae enlaces u otros tipos no permitidos"
  mkdir "$d/x"
  tar -xf "$d/in.tar" -C "$d/x" --no-same-owner
  mkdir -p scripts
  # cp + mv (inodo nuevo): no se reescribe en sitio un script que bash puede estar leyendo
  cp -- "$d/x/docker-compose.yml" docker-compose.yml.new && mv -f docker-compose.yml.new docker-compose.yml
  for f in "$d"/x/scripts/*; do
    [ -f "$f" ] || continue
    b=${f##*/}
    cp -- "$f" "scripts/$b.new" && mv -f "scripts/$b.new" "scripts/$b"
    case "$b" in *.sh) chmod 755 "scripts/$b" ;; esac
  done
  echo "infraestructura actualizada"
}

cmd_up() {
  local tag="${1:-}" cur prev
  tag_ok "$tag"
  detach up "$tag"
  lock
  cur=$(read_tag .current_tag)
  prev=$(read_tag .previous_tag)
  export APP_TAG="$tag"
  check_images
  if [ -n "$cur" ] && ! docker image inspect "shipbender:$cur" >/dev/null 2>&1; then
    echo "AVISO: la imagen actual shipbender:$cur ya no esta: no habra rollback automatico" >&2
  fi
  backup_if_running
  if [ "$cur" = "$tag" ]; then save_tags "$tag" "$prev"; else save_tags "$tag" "$cur"; fi
  if up_and_wait; then echo "ok: shipbender:$tag healthy (anterior: ${cur:-ninguna})"; return 0; fi
  echo "ERROR: shipbender:$tag no llego a healthy" >&2
  docker compose logs --tail 30 app >&2 || true
  if [ -n "$cur" ] && [ "$cur" != "$tag" ]; then
    echo "volviendo automaticamente a shipbender:$cur" >&2
    save_tags "$cur" "$prev"
    export APP_TAG="$cur"
    up_and_wait || die "CRITICO: tampoco $cur llega a healthy. Revisa: scripts/deploy.sh status"
    echo "rollback automatico ok: corre $cur. Si $tag traia una migracion V*__ nueva, restaura el dump previo (scripts/restore.sh)." >&2
  else
    echo "no hay version anterior a la que volver" >&2
  fi
  exit 1
}

cmd_rollback() {
  local tag cur
  detach rollback
  lock
  tag=$(read_tag .previous_tag)
  cur=$(read_tag .current_tag)
  [ -n "$tag" ] || die "no hay version anterior (.previous_tag vacio)"
  export APP_TAG="$tag"
  check_images
  backup_if_running
  save_tags "$tag" "$cur"   # swap: otro rollback vuelve a la de ahora
  up_and_wait || die "shipbender:$tag no llega a healthy (revisa: scripts/deploy.sh status)"
  echo "rollback ok: corre $tag. Si la version que dejas traia una migracion V*__ nueva, restaura el dump previo (scripts/restore.sh)."
}

cmd_status() {
  local cur; cur=$(read_tag .current_tag)
  echo "actual:   ${cur:-ninguna}"
  echo "anterior: $(read_tag .previous_tag)"
  APP_TAG="${cur:-none}" docker compose ps
  APP_TAG="${cur:-none}" docker compose logs --tail 20 app
}

reject() { echo "comando no permitido" >&2; exit 126; }
cmd_ssh_entry() {
  local c="${SSH_ORIGINAL_COMMAND:-}"
  local re_load='^load ([0-9a-f]{7,12})$' re_up='^up ([0-9a-f]{7,12})$'
  [ -n "$c" ] && [[ "$c" != *$'\n'* && "$c" != *$'\r'* ]] || reject
  if [[ "$c" =~ $re_load ]]; then cmd_load "${BASH_REMATCH[1]}"
  elif [[ "$c" =~ $re_up ]]; then cmd_up "${BASH_REMATCH[1]}"
  elif [ "$c" = rollback ]; then cmd_rollback
  elif [ "$c" = status ]; then cmd_status
  elif [ "$c" = infra ]; then cmd_infra
  else reject
  fi
}

main() {
  case "${1:-}" in
    load) cmd_load "${2:-}" ;;
    infra) cmd_infra ;;
    up) cmd_up "${2:-}" ;;
    rollback) cmd_rollback ;;
    status) cmd_status ;;
    ssh-entry) cmd_ssh_entry ;;
    *) die "uso: deploy.sh load <tag> | infra | up <tag> | rollback | status | ssh-entry" ;;
  esac
}
main "$@"
exit
