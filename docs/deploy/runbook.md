# Runbook de operación — The Last Shipbender

Servidor: Docker Compose (db + app + tunnel + backup) detrás de Cloudflare Tunnel en `https://shipbender.racnar.dev`.
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

## 1. Primera instalación

Requisitos: Docker con Compose v2, git, `openssl`. Antes, una sola vez, en el panel de Cloudflare:

1. Zero Trust → *Networks → Tunnels → Create a tunnel* → *Cloudflared* → nombre `shipbender` → copiar el **token** (no ejecutar el comando de instalación que ofrece).
2. En el túnel → *Public Hostname*: subdominio `shipbender`, dominio `racnar.dev`, tipo `HTTP`, URL `app:8080`, sin *HTTP Host Header*.
3. Dashboard de `racnar.dev`: *SSL/TLS → Edge Certificates → Always Use HTTPS* activado; *Network → WebSockets* activado.
4. (Obligatorio) *Security → WAF → Rate limiting rules*: 1 regla para `POST` a `/api/auth/login` y `/api/players`.

En el servidor:

```bash
git clone <url-del-repo> shipbender && cd shipbender   # rama main
cp .env.example .env
openssl rand -base64 48     # ejecútalo dos veces: una para JWT_SECRET y otra para POSTGRES_PASSWORD
```

Edita `.env` y completa `POSTGRES_PASSWORD`, `JWT_SECRET`, `TUNNEL_TOKEN` y `BACKUP_DIR` (carpeta **fuera del repo**, ver sección 0; créala antes). `.env` está ignorado por git: no lo subas ni lo pegues en chats o tickets. Luego:

```bash
docker compose config -q                 # valida .env (si falta un secreto, lo dice)
docker compose up -d --build             # la primera vez tarda varios minutos
docker compose ps                        # db y app "healthy"; tunnel "Up"
docker compose logs --tail 50 tunnel     # debe mostrar conexiones registradas con Cloudflare
```

Comprobar: abrir `https://shipbender.racnar.dev`, registrarse, iniciar sesión y entrar a una partida (chat/emotes usan WebSocket). Prod no crea usuarios por defecto: el primero lo registras tú.
Comprobación sin pasar por Cloudflare (desde el servidor):

```bash
docker compose exec app wget -qO- http://localhost:8080/ | head -c 200
```

Después reinicia el servidor una vez y comprueba que el juego vuelve solo (secciones 0 y "Problemas conocidos").

## 2. Estado y logs

```bash
docker compose ps
docker compose logs -f --tail 200 app        # también: tunnel, backup, db (Ctrl+C para salir)
docker compose logs --tail 50 db backup      # varios servicios a la vez
```

## 3. Actualizar a una versión nueva

Cada versión es una imagen `shipbender:<APP_TAG>` con el sha corto de git.

```bash
docker image ls shipbender                                 # anota el APP_TAG actual (para el rollback)
git pull
docker compose exec backup sh /scripts/backup.sh           # backup previo; anota el nombre del dump
sed -i "s/^APP_TAG=.*/APP_TAG=$(git rev-parse --short HEAD)/" .env   # o edita APP_TAG a mano
docker compose up -d --build app
docker compose ps                                          # app "healthy" (puede tardar ~1 min)
docker compose logs --tail 50 app                          # sin errores de Flyway ni de arranque
```

Probar el login en `https://shipbender.racnar.dev`.

## 4. Rollback

```bash
docker image ls shipbender                                  # tags disponibles (siguen en local)
sed -i "s/^APP_TAG=.*/APP_TAG=<tag-anterior>/" .env
docker compose up -d app
docker compose ps
```

Si la versión nueva traía una migración `V*__` nueva en `src/main/resources/db/migration` (Flyway solo avanza: la BD ya tiene el esquema nuevo), **antes** restaura el dump previo a la actualización con `scripts/restore.sh <dump>` (sección 5) y luego levanta el tag anterior como arriba. Se pierden los datos creados desde ese backup.

## 5. Backups y restauración

El servicio `backup` hace un dump al arrancar y cada 24 h en `BACKUP_DIR` (`shipbender-AAAAMMDD-HHMMSS.dump`) y borra los de más de `RETENTION_DAYS` (14) días.

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
docker compose up -d --build            # crea app y tunnel
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

- **El WebSocket (chat/partida) da 403 tras el túnel**: Tomcat debe ver `X-Forwarded-Proto: https` y el `Host` original (`server.forward-headers-strategy=native`, ADR 007 punto 6). Revisa `docker compose logs app`, que el hostname del túnel apunte a `app:8080` sin *HTTP Host Header* y que *Network → WebSockets* esté activado. Plan B del ADR: `allowedOriginPatterns` con el origen público.
- **Git Bash convierte rutas** (`/backups/...` pasa a `C:/Program Files/Git/backups/...`): ejecuta `export MSYS_NO_PATHCONV=1` en cada sesión nueva.
- **Tras reiniciar el servidor el juego no vuelve** hasta que Docker esté en marcha (Windows: hasta que alguien inicie sesión; U7). Comprueba `docker compose ps`.
- **`up` falla con "container db is unhealthy" en un arranque en frío lento**: Postgres tarda ~20 s en inicializar el clúster; el healthcheck de `db` tiene `start_period: 60s` para tolerarlo. Si persiste, mira `docker compose logs db` (disco o CPU saturados).
- **`docker compose` dice `falta X en .env`**: falta un secreto en `.env`; es intencional, el stack no arranca sin ellos.
- **`tunnel` en `Restarting`** con "Provided Tunnel token is not valid": `TUNNEL_TOKEN` mal copiado o caducado; ver sección 6.
- **El dump no aparece en el host (Windows)**: `BACKUP_DIR` es una ruta de Git Bash (`/tmp/...`) en vez de una ruta de Windows (`D:/...`).
