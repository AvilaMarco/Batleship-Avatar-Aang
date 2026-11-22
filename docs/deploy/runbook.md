# Runbook de operación — The Last Shipbender

Servidor: Debian 13 con Docker, Caddy y el túnel de Cloudflare que ya existe. Stack `compose.yaml` (`app` + `db`), con el paquete `recibir` y el runner del administrador. Publicado en `https://shipbender.racnar.dev`. Contrato y motivos: `docs/adr/007-deploy.md` (actualización 2026-11-22) y `docs/deploy/PRIMER-DESPLIEGUE.md`.

> **ADVERTENCIA — pierdes todos los datos**
> Nunca ejecutes `docker compose down -v`, `docker volume prune` ni `docker system prune --volumes` en el servidor: borran la base de datos (jugadores y partidas). Tampoco `docker image prune -a` (el rollback necesita las imágenes anteriores).

## 1. Requisitos previos (una vez, antes del primer despliegue)

Lo hace el administrador y se comprueba en `PRIMER-DESPLIEGUE.md` (pre-requisitos):

- `postgres:17.11-alpine` (tag exacto) precargada en Docker. El paquete no la lleva y el servidor no hace pull.
- `data/pgdata` con dueño `70:70` y modo `0700` (`install -d -o 70 -g 70 -m 0700 data/pgdata`). Sin eso Postgres no arranca.
- Publicación: la app escucha solo en `127.0.0.1:<APP_PORT>` (asignado por el administrador con `server-app-public`). Caddy reparte y **fija `X-Forwarded-Proto: https`** (sin eso el WebSocket da 403).
- Cloudflare: ruta del hostname hacia Caddy, política Access **Bypass** para `shipbender.racnar.dev` (el juego es público), WebSockets activados y la regla de rate limiting (10 peticiones / 10 s por IP) sobre `POST /api/auth/login` y `POST /api/players`.

## 2. Desplegar una versión

Desde la PC de desarrollo (Git Bash), en la rama `main`, con el árbol limpio:

```bash
cp .ship.env.example .ship.env     # una vez: SHIP_HOST (alias ssh, p. ej. tva-deploy); no versionado
scripts/ship.sh --dry-run          # compila y valida el paquete, no envía
scripts/ship.sh                    # compila shipbender/app:<sha>, envía por SSH: ssh tva-deploy recibir shipbender <sha>
```

`ship.sh` arma un paquete con tres ficheros (`servidor.env`, `compose.yaml`, `imagenes.tar.gz`). Cada versión es `shipbender/app:<sha corto de git>`. `${VERSION}` del compose lo fija el runner al hacer `up`.

Códigos de salida de `ship.sh` (los devuelve `recibir`):

| Código | Significado | Qué hacer |
|---|---|---|
| 0 | Desplegado y sano | Nada |
| 1 | Falló y el servidor volvió a la versión anterior | Mirar los logs del servidor (`app`) y corregir antes de reintentar |
| 2 | Paquete rechazado (formato o validación) | Revisar `servidor.env` / `compose.yaml` |
| 3 | Pendiente de aprobación del admin (el compose cambió respecto del aprobado) | El admin ejecuta `sudo server-app-aprobar shipbender`; no reenvíes en bucle |
| 4 | Se cortó la espera del log; el despliegue sigue | Consultar el estado (paso 4) antes de reintentar |

## 3. Estado y logs

Desde el servidor, con el comando que tenga el administrador para la app (`estado shipbender`, `server-logs app-shipbender`). Qué mirar:

- `app` y `db` en `healthy`.
- `app` responde en `127.0.0.1:<APP_PORT>/` con 200.
- WebSocket: `curl` con `Upgrade: websocket`, `Origin: https://shipbender.racnar.dev` y `X-Forwarded-Proto: https` debe dar 101; con origen ajeno o sin esa cabecera, 403.

Comprobación pública: `curl -sI https://shipbender.racnar.dev/` debe dar 200 de la app, **no** un 302 a `cloudflareaccess.com`.

## 4. Rollback

El runner del administrador vuelve a la imagen anterior (conserva 3 versiones) si la nueva no llega a `healthy` en 150 s (`SALUD_ESPERA`). Para volver a mano, pídelo al administrador o usa su comando de rollback; no hagas `docker compose up` a mano.

**Un rollback solo cambia la imagen. No deshace migraciones.** Flyway solo avanza. Hay que tenerlo en cuenta:

- Las migraciones son expand/contract: una versión nueva puede añadir columnas, valores o constraints que la versión anterior no conoce.
- **Migración V4 (LOTUS)**: amplía los checks `player_nation_check` y `game_nation_check` con `'LOTUS'`. Antes de volver a una versión anterior a V4, quita esos valores (si no, la versión anterior no puede leer las filas):

  ```sql
  update player set nation = null where nation = 'LOTUS';
  update game   set nation = null where nation = 'LOTUS';
  ```

  Comprueba antes qué hace la versión anterior con `nation` nulo. Si no es válido, la alternativa es restaurar el backup.
- Si una migración nueva no es compatible hacia atrás, la vuelta atrás exige restaurar el dump previo al despliegue (paso 5). Se pierden los datos creados desde ese backup.

Regla para los equipos: no se hace una migración destructiva (drop, renombrado, cambio de tipo) en el mismo despliegue que deja de usarse la versión anterior. Primero expand, luego contract, en despliegues distintos.

## 5. Backups y restauración

El runner del administrador hace un backup antes de cada `up` (ADR 007), y el administrador tiene su propio `server-backup`; el procedimiento exacto (carpeta, retención, dónde se copian) lo fija él (PRIMER-DESPLIEGUE, fila H21-22). La restauración también la hace el administrador: sobrescribe los datos actuales, así que se decide con él. Los dumps contienen hashes de contraseñas: nada de copias en nube sin cifrar.

Prueba periódica: restaurar un dump en un proyecto de prueba. Un backup sin probar no es un backup.

## 6. Rotar secretos

- `JWT_SECRET` y `POSTGRES_PASSWORD` los genera el servidor (`SECRETOS` en `deploy/servidor.env`, hex). El repo no tiene secretos reales: solo `.env.example` con valores de ejemplo.
- Rotar `JWT_SECRET` cierra todas las sesiones: lo pide el administrador.
- **`POSTGRES_PASSWORD`** no basta con cambiarlo en la configuración: Postgres conserva la contraseña vieja en `pgdata`. Requiere un procedimiento coordinado con el administrador (cambiar la contraseña en Postgres y en la configuración a la vez); no está automatizado en el repo.

## 7. Problemas conocidos

- **WebSocket 403 tras Cloudflare**: Caddy no fija `X-Forwarded-Proto: https`, o falta `server.forward-headers-strategy=native` (ADR 007). El resto del juego puede cargar igual.
- **`curl` a la URL pública da 302 a `cloudflareaccess.com`**: falta la política Bypass del hostname.
- **`db` no arranca / permisos**: `data/pgdata` no es de `70:70` con modo `0700`.
- **Despliegue falla al crear `db` por imagen**: `postgres:17.11-alpine` no está precargada en el servidor.
- **`db` `unhealthy` en arranque en frío**: Postgres tarda hasta ~20 s; el healthcheck tiene `start_period: 60s`. Si persiste, mira los logs de `db` (disco o CPU saturados).
- **Tras reiniciar el servidor el juego no vuelve**: comprueba que `docker` y los contenedores arrancan (`restart: unless-stopped`); no debe requerir iniciar sesión.
- **Git Bash convierte rutas** (`/tmp/...`): ejecuta `export MSYS_NO_PATHCONV=1` en cada sesión nueva.

## 8. Reglas de seguridad

- No arranques la imagen con `docker run` sin `SPRING_PROFILES_ACTIVE=prod`: la imagen ya lo fija, pero no lo sobrescribas.
- La clave SSH de despliegue equivale a acceso de administrador al servidor (grupo `docker`): solo para desplegar, con `from=` y `restrict` en `authorized_keys`, gestionada por el administrador.
