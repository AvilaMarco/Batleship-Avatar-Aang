# Runbook de operación — The Last Shipbender

Servidor: Docker Compose (db + app + backup; `tunnel` solo opcional) publicado en `https://shipbender.racnar.dev` detrás de Caddy y del túnel que ya tiene el servidor (sección 1).
Todo se ejecuta **desde la raíz del clon de producción** (rama `main`, nunca la copia de desarrollo). Detalle y motivos: `docs/adr/007-deploy.md`.

> **ADVERTENCIA — pierdes todos los datos**
> Nunca ejecutes `docker compose down -v`, `docker volume rm shipbender_pgdata`, `docker volume prune` ni `docker system prune --volumes`: borran el volumen `pgdata` (jugadores y partidas). `docker compose down` **sin** `-v` es seguro.

## 0. Particularidades por sistema operativo (pendiente de confirmar el SO del servidor)

Aún no se sabe qué SO tiene el servidor. Solo se documenta lo ya conocido; completar al confirmarlo.

| Tema | Windows + Docker Desktop | Linux + Docker Engine |
|---|---|---|
| Shell para los comandos | Git Bash, con `export MSYS_NO_PATHCONV=1` | cualquier shell POSIX (bash) |
| Arranque tras reinicio | *Start Docker Desktop when you sign in*: arranca al **iniciar sesión**, no al encender (U7). No suspender con corriente | `sudo systemctl enable docker` + `restart: unless-stopped` (ya está en el compose) |
| `BACKUP_DIR` | ruta de Windows con barras normales, p. ej. `D:/backups/shipbender`. **No** una ruta de Git Bash como `/tmp/...`: Docker Desktop la toma como ruta de su VM y el dump no aparece en el disco | ruta absoluta, p. ej. `/srv/backups/shipbender` |
| Pendiente de confirmar | versión de Docker/Compose, RAM (Docker Desktop >= 4 GB), disco de backups | versión de Docker/Compose (plugin `docker compose`), si hace falta `sudo` o el grupo `docker`, RAM, disco de backups |

## 1. Primera instalación (servidor real: detrás de Caddy)

Requisitos: Docker con Compose v2, git, `openssl`.

### Publicación detrás de Caddy

Flujo: Cloudflare → `cloudflared` (Docker, red `tunel`) → listener público de Caddy (`10.240.140.1:8080`) → `reverse_proxy 127.0.0.1:APP_PORT` → contenedor `app`. La app publica **solo** en `127.0.0.1:APP_PORT` (nunca `0.0.0.0`); `db` y `backup` no publican nada. El servidor ya tiene su túnel: **no se levanta uno propio** (el perfil `tunnel` queda desactivado).

El administrador del servidor debe:
1. Asignar `APP_PORT` (fuera del rango 4700-4759, de desarrollo).
2. Dar de alta el hostname `shipbender.racnar.dev` con su herramienta y la ruta de Cloudflare hacia Caddy.
3. **Cloudflare Access**: el juego es público; si `*.racnar.dev` está cerrado por defecto, `shipbender.racnar.dev` necesita una política **Bypass**.
4. En Caddy, para este hostname, preservar `Host` (es el comportamiento por defecto) y **fijar `X-Forwarded-Proto: https`**, p. ej. `reverse_proxy 127.0.0.1:APP_PORT { header_up X-Forwarded-Proto https }`. Caddy termina el HTTP de cloudflared y sin esa cabecera Tomcat cree que la petición es `http` y rechaza el WebSocket (ver la comprobación abajo).
5. Dashboard de `racnar.dev`: *SSL/TLS → Edge Certificates → Always Use HTTPS* activado; *Network → WebSockets* activado.
6. (Obligatorio) *Security → WAF → Rate limiting rules*: 1 regla para `POST` a `/api/auth/login` y `/api/players`.

En el servidor:

```bash
git clone <url-del-repo> shipbender && cd shipbender   # rama main
cp .env.example .env
openssl rand -base64 48     # ejecútalo dos veces: una para JWT_SECRET y otra para POSTGRES_PASSWORD
```

Edita `.env` y completa `POSTGRES_PASSWORD`, `JWT_SECRET`, `APP_PORT` y `BACKUP_DIR` (carpeta **fuera del repo**, ver sección 0; créala antes). `TUNNEL_TOKEN` se deja vacío. `.env` está ignorado por git: no lo subas ni lo pegues en chats o tickets. Luego:

```bash
docker compose config -q                 # valida .env (si falta un secreto o APP_PORT, lo dice)
docker compose up -d --build             # SOLO máquinas con Internet. El servidor real no compila: usa scripts/ship.sh --with-db desde la PC de desarrollo (sección 3)
docker compose ps                        # db y app "healthy"; app publicada en 127.0.0.1:APP_PORT
```

Comprobar sin pasar por Cloudflare (desde el servidor; el handshake es tal como lo envía Caddy):

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:$APP_PORT/        # 200
H=(-H 'Host: shipbender.racnar.dev' -H 'Connection: Upgrade' -H 'Upgrade: websocket' -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==')
U=http://127.0.0.1:$APP_PORT/the-last-shipbender
curl -s -o /dev/null -w '%{http_code}\n' "${H[@]}" -H 'Origin: https://shipbender.racnar.dev' -H 'X-Forwarded-Proto: https' $U   # 101
curl -s -o /dev/null -w '%{http_code}\n' "${H[@]}" -H 'Origin: https://evil.example' -H 'X-Forwarded-Proto: https' $U         # 403 (origen ajeno)
curl -s -o /dev/null -w '%{http_code}\n' "${H[@]}" -H 'Origin: https://shipbender.racnar.dev' -H 'X-Forwarded-Proto: http' $U # 403
```

El último caso (igual si la cabecera no llega) es lo que pasa si Caddy **no** fija `X-Forwarded-Proto: https`: el WebSocket falla con 403 aunque el resto del juego cargue. Verificado en la tarea 610. Prueba de la cadena real: abrir `https://shipbender.racnar.dev`, registrarse, iniciar sesión y entrar a una partida (chat/emotes). Prod no crea usuarios por defecto: el primero lo registras tú.

Después reinicia el servidor una vez y comprueba que el juego vuelve solo (secciones 0 y "Problemas conocidos").

### Túnel propio (opcional, solo otras máquinas)

Solo si la máquina no tiene ya un túnel/proxy. Antes, una sola vez, en el panel de Cloudflare:

1. Zero Trust → *Networks → Tunnels → Create a tunnel* → *Cloudflared* → nombre `shipbender` → copiar el **token** (no ejecutar el comando de instalación que ofrece).
2. En el túnel → *Public Hostname*: subdominio `shipbender`, dominio `racnar.dev`, tipo `HTTP`, URL `app:8080`, sin *HTTP Host Header*.
3. Los pasos 5 y 6 de la lista anterior (HTTPS/WebSockets, rate limiting).

Pon `TUNNEL_TOKEN` en `.env` y arranca con `docker compose --profile tunnel up -d --build`; `docker compose logs --tail 50 tunnel` debe mostrar conexiones registradas. Compose interpola también los servicios de perfiles inactivos, por eso `TUNNEL_TOKEN` no usa `:?`: si queda vacío, `tunnel` falla al arrancar con "token is not valid".

## 2. Estado y logs

```bash
docker compose ps
docker compose logs -f --tail 200 app        # también: tunnel, backup, db (Ctrl+C para salir)
docker compose logs --tail 50 db backup      # varios servicios a la vez
```

## 3. Desplegar una versión

El servidor **no compila ni hace pull** (solo sale a Internet por el túnel): la imagen se compila en la PC de desarrollo y se envía por SSH. Cada versión es `shipbender:<sha corto de git>`.

Desde la PC de desarrollo (Git Bash/WSL), en `main` y con el árbol limpio:

```bash
cp .ship.env.example .ship.env      # una vez: SHIP_HOST, SHIP_USER, SHIP_KEY (no versionado)
scripts/ship.sh --with-db           # la primera vez (incluye postgres:17-alpine); después: scripts/ship.sh
```

`ship.sh` compila (`linux/amd64`), envía imagen + `docker-compose.yml` + `scripts/` y ejecuta `deploy.sh up <tag>` en el servidor, que: hace backup si hay una versión corriendo (Flyway no se deshace: el dump es la salvaguarda), guarda `.current_tag`/`.previous_tag`, levanta con `--no-build` y espera `healthy` (máx. 180 s). Si no llega, **vuelve solo a la versión anterior** y termina con error (si la nueva traía una migración `V*__`, restaura el dump con `scripts/restore.sh`, sección 5). Un fallo causado por `.env` (p. ej. `JWT_SECRET` corto) afecta también a la versión anterior: no hay rollback que lo arregle; corrige `.env` y repite `deploy.sh up <tag>`.

En el servidor, usa siempre `scripts/deploy.sh` (`status`, `rollback`, `up <tag>`) y no `docker compose up` a mano: `APP_TAG` lo fija `deploy.sh`, el de `.env` no es la versión real. Las imágenes viejas se acumulan (51 GB libres): borra a mano las antiguas con `docker image rm shipbender:<tag>`, nunca `prune -a` (el rollback las necesita).

### Clave SSH restringida (la instala el administrador del servidor)

En la PC de desarrollo: `ssh-keygen -t ed25519 -f ~/.ssh/shipbender_deploy` (solo para desplegar). En el servidor, en `~/.ssh/authorized_keys` del usuario de despliegue (que debe poder usar docker), una línea:

```
restrict,command="/ruta/al/clon/scripts/deploy.sh ssh-entry" ssh-ed25519 AAAA... deploy-shipbender
```

`ssh-entry` solo acepta `load <tag>`, `up <tag>`, `rollback`, `status` e `infra` (el tag debe cumplir `^[0-9a-f]{7,12}$`); cualquier otra cosa se rechaza sin ejecutar nada. La clave privada no sale de tu PC. **La clave de despliegue equivale a root del servidor**: el grupo `docker` es root, `infra` sustituye el compose y `load` acepta cualquier imagen. Por eso: clave solo para este fin, `from="IP-de-tu-PC"` delante de `restrict` en `authorized_keys` (`from="1.2.3.4",restrict,command="..." ssh-ed25519 ...`), y un usuario de despliegue sin sudo ni shell interactivo. Protégela como una credencial de producción.

**Si se corta el SSH a mitad de un despliegue**: `up` y `rollback` siguen hasta el final (ignoran HUP/PIPE y escriben en `.deploy.log`, 0600, en la raíz del clon). Al reconectar mira `scripts/deploy.sh status` y `tail -n 30 .deploy.log`: ahí consta si llegó a healthy o si hizo rollback. Topes: `infra` 8 MiB y `load` 2 GiB (comprimido); `load` e `infra` también toman el lock.

## 4. Rollback

```bash
scripts/deploy.sh status      # versión actual y anterior
scripts/deploy.sh rollback    # intercambia actual y anterior (hace backup antes); otro rollback vuelve a la de ahora
```

(Remoto: `ssh -i <clave> usuario@servidor rollback`.) Si la versión que dejas traía una migración `V*__` nueva (Flyway solo avanza), restaura **después** el dump previo con `scripts/restore.sh <dump>` (sección 5): se pierden los datos creados desde ese backup.

## 5. Backups y restauración

El servicio `backup` hace un dump al arrancar y cada 24 h en `BACKUP_DIR` (`shipbender-AAAAMMDD-HHMMSS.dump`) y borra los de más de `RETENTION_DAYS` (14) días. Además `deploy.sh up` hace un dump antes de cada despliegue: con muchos despliegues al día se acumulan hasta que los barre la retención. Los dumps son 0600 de root: el usuario de despliegue no puede leerlos (usa sudo/root del administrador para restaurar o copiarlos).

```bash
docker compose exec backup sh /scripts/backup.sh     # backup a mano
docker compose exec backup ls -l /backups            # ver los dumps (o mira BACKUP_DIR en el host)
scripts/restore.sh shipbender-AAAAMMDD-HHMMSS.dump   # solo el nombre; el archivo debe estar en BACKUP_DIR
```

`restore.sh` valida el dump, pide escribir `si`, para `app`, guarda un dump de seguridad de lo actual, restaura en una sola transacción (si falla, la BD queda intacta) y arranca `app` de nuevo. **Reemplaza los datos actuales.** En Windows, ejecútalo desde Git Bash.

**Máquina nueva / desastre**: haz la instalación (sección 1) hasta crear `.env`, y entonces:

```bash
docker compose up -d db backup          # BD vacía
# copia el dump a BACKUP_DIR
scripts/restore.sh <dump>               # avisará que app NO se arrancó: aún no existe, es normal
docker compose up -d --build            # crea app (y tunnel con --profile tunnel)
```

### Carpeta, vigilancia y pruebas

- **Carpeta privada**: `BACKUP_DIR` debe ser privada, fuera del repo y en otro disco. Los dumps contienen hashes de contraseña: no la pongas en OneDrive u otra nube sin cifrar.
- **Saber si falla**: `docker compose logs backup` debe mostrar `backup ok: ...` cada 24 h; revísalo de vez en cuando.
- **Prueba de restauración**: cada cierto tiempo restaura un dump en una máquina o proyecto de prueba (`COMPOSE_PROJECT_NAME` distinto); un backup sin probar no es un backup.

## 6. Rotar secretos

- **`JWT_SECRET`** (cierra todas las sesiones): genera uno con `openssl rand -base64 48`, edítalo en `.env`, `docker compose up -d app`.
- **Token del túnel**: en Zero Trust, Networks → Tunnels → tu túnel → Refresh token; edita `TUNNEL_TOKEN` en `.env`, `docker compose up -d tunnel`.
- **`.env`**: que solo lo lea tu usuario (Linux: `chmod 600 .env`; Windows: quita permisos a otros usuarios).
- **`POSTGRES_PASSWORD`**: no basta con editar `.env` (Postgres conserva la contraseña vieja en `pgdata`). No está cubierto por este runbook.

## Reglas de seguridad obligatorias

- **No arranques contenedores sueltos con `docker run`** de la imagen sin `SPRING_PROFILES_ACTIVE=prod`: el perfil por defecto es dev.
- **La regla de rate limiting de Cloudflare es obligatoria** (ver sección 1, paso 4); créala antes de abrir el juego.

## 7. Parar y arrancar

```bash
docker compose down        # quita contenedores y redes; el volumen pgdata (datos) se conserva
docker compose up -d       # vuelve a arrancar con los datos intactos
```

## Problemas conocidos

- **El WebSocket (chat/partida) da 403 tras el túnel**: Tomcat debe ver `X-Forwarded-Proto: https` y el `Host` original (`server.forward-headers-strategy=native`, ADR 007 punto 6). Revisa `docker compose logs app`, que Caddy fije `X-Forwarded-Proto: https` y preserve `Host` (sección 1, comprobación con curl) y que *Network → WebSockets* esté activado (con túnel propio: hostname a `app:8080` sin *HTTP Host Header*). Plan B del ADR: `allowedOriginPatterns` con el origen público.
- **Git Bash convierte rutas** (`/backups/...` pasa a `C:/Program Files/Git/backups/...`): ejecuta `export MSYS_NO_PATHCONV=1` en cada sesión nueva.
- **Tras reiniciar el servidor el juego no vuelve** hasta que Docker esté en marcha (Windows: hasta que alguien inicie sesión; U7). Comprueba `docker compose ps`.
- **`up` falla con "container db is unhealthy" en un arranque en frío lento**: Postgres tarda ~20 s en inicializar el clúster; el healthcheck de `db` tiene `start_period: 60s` para tolerarlo. Si persiste, mira `docker compose logs db` (disco o CPU saturados).
- **`docker compose` dice `falta X en .env`**: falta un secreto en `.env`; es intencional, el stack no arranca sin ellos.
- **`tunnel` en `Restarting`** con "Provided Tunnel token is not valid": `TUNNEL_TOKEN` mal copiado o caducado; ver sección 6.
- **El dump no aparece en el host (Windows)**: `BACKUP_DIR` es una ruta de Git Bash (`/tmp/...`) en vez de una ruta de Windows (`D:/...`).
