# ADR 007 — Deploy: Docker Compose + Cloudflare Tunnel (F6)

Estado: **aprobada con decisiones del usuario (2026-11-21)** · Implementa ADR 001 punto 5. Front dentro del jar (ADR 006 punto 7), Postgres + Flyway (ADR 003 punto 4).

## Contexto
Una sola persona opera el juego en un PC con Windows 11 + Docker Desktop, que también se usa para desarrollar (Testcontainers, puertos 4703-4705). El dominio `racnar.dev` ya está en Cloudflare. No hay datos que conservar. Hoy: `spring.profiles.default=dev` (`application.properties:23`), el perfil prod solo exige `JWT_SECRET` y apaga springdoc, el WebSocket usa la comprobación de mismo origen implícita de Spring y la CSP permite `ws: wss:` hacia cualquier host.

## Decisiones
1. **Imagen** (Anexo A), multi-stage: `node:22-slim` (`npm ci` + `npm run build`), `eclipse-temurin:21-jdk-alpine` (`mvnw package -DskipTests`, con el `dist` copiado a `src/main/resources/static`) y `eclipse-temurin:21-jre-alpine` con usuario `app` no root. Caché con `RUN --mount=type=cache` de BuildKit para `~/.npm` y `~/.m2`, y `package*.json` copiados antes que el código. El perfil Maven `frontend` no se usa en la imagen (el stage de Node ya construye) y el `pom.xml` no cambia. Los tests no corren en la imagen: necesitan Docker y se corren antes (`./mvnw test`). `ENV SPRING_PROFILES_ACTIVE=prod` en la imagen (si falta el perfil, Spring arranca `dev` con el seeder; ver nota 1).
2. **Healthcheck sin actuator**: `wget -q --spider http://localhost:8080/` (busybox de Alpine) contra el `index.html` público. Docker Compose no reinicia contenedores `unhealthy`: el healthcheck solo sirve para ver el estado y para ordenar el arranque (`depends_on: service_healthy`). Si la BD cae, Hikari reconecta solo. Agregar actuator para eso suma una dependencia, una ruta pública y su exposición: no aporta nada que se use.
3. **Compose** (Anexo B): `db` (`postgres:17-alpine`, volumen con nombre `pgdata`), `app`, `tunnel` (`cloudflare/cloudflared` con versión fija, nunca `latest`), `backup`. **Ningún servicio tiene `ports:`.** Dos redes: `backend` (`internal: true`: db, app, backup; sin salida a Internet) y `edge` (app, tunnel; cloudflared necesita salir). `restart: unless-stopped`, `mem_limit` (db 512m, app 768m con `-XX:MaxRAMPercentage=75`, tunnel 128m, backup 128m), logs `json-file` con rotación 3×10 MB. Nombre de proyecto fijo `shipbender`.
4. **Secretos** (Anexo C): `.env` (ya ignorado por git) + `.env.example` versionado con las claves y sin valores secretos. Compose usa `${VAR:?mensaje}` para que falte `POSTGRES_PASSWORD`, `JWT_SECRET` o `TUNNEL_TOKEN` y no arranque. **`JWT_SECRET` nuevo** (el viejo está en el historial de git) generado con `openssl rand -base64 48` (64 caracteres; `JwtService` ya rechaza menos de 32). Contraseña de Postgres generada igual. `JWT_EXPIRATION_MINUTES=480` (8 h, ver punto 9).
5. **Cloudflare Tunnel** con token (túnel gestionado desde el panel): `cloudflared tunnel --no-autoupdate run` lee `TUNNEL_TOKEN`. El hostname público apunta a `http://app:8080` y la ruta se configura en el panel, no en archivos. No se abre ningún puerto del router ni del host. TLS lo termina Cloudflare y el tramo cloudflared → app va por la red interna de Docker en HTTP.
6. **WebSocket detrás del túnel**: cloudflared reenvía el `Host` original y agrega `X-Forwarded-Proto: https`. Sin más configuración Tomcat ve `http`, y `WebUtils.isSameOrigin` compara esquema y puerto (`http:80` contra `Origin: https://…:443`), así que **el handshake daría 403**. Se arregla con `server.forward-headers-strategy=native` en `application.properties` (todos los perfiles): el `RemoteIpValve` de Tomcat solo confía en `X-Forwarded-*` si vienen de IPs privadas (la red de Docker o localhost), y con eso también se loguea la IP real del cliente. No se declara `allowedOriginPatterns`: el mismo origen ya es la regla correcta y así no hay que mantener el dominio en otra variable. Plan B, solo si la verificación de 607 falla: `setAllowedOriginPatterns("${app.public-origin}")` por variable.
7. **CSP**: `connect-src 'self' ws: wss:` → `connect-src 'self'`. En CSP nivel 3 (navegadores actuales) `'self'` ya cubre `wss://` al mismo host, y `ws: wss:` deja que un XSS mande el token de `localStorage` por WebSocket a cualquier servidor. No hace falta el dominio. HSTS lo pone Spring Security en cuanto la petición se ve segura (efecto del punto 6). Además `.dev` está en la lista HSTS precargada de los navegadores.
8. **Prod sin extras**: springdoc ya está apagado en `application-prod.properties` (`/swagger` y `/docs` dan 404). `DevDataSeeder` es `@Profile("dev")` y la imagen fija `prod`, así que no se crea ningún usuario por defecto (se verifica con `count(*)` = 0 en 603). El registro (`POST /api/players`) sigue abierto porque es la única forma de tener jugadores (decisión U6). devtools no entra en el jar repackageado (se verifica en 601).
9. **Expiración del JWT: 8 h.** No hay revocación ni refresh y el token vive en `localStorage`: el tiempo de vida es la ventana de abuso si lo roban. 8 h cubren una tarde de juego. Al caducar, el front ya vuelve al login (tareas 503/513). Se descarta 24 h (ventana 3 veces mayor sin beneficio para partidas de minutos) y 1 h (cortaría a quien juega varias partidas seguidas, porque no hay refresh).
10. **Backups** (Anexo D): el servicio `backup` (misma imagen `postgres:17-alpine`, red `backend`) corre `scripts/backup.sh` al arrancar y luego cada 24 h. Hace `pg_dump -Fc` en `${BACKUP_DIR}` (montado del host) y borra los dumps de más de `RETENTION_DAYS` (14) días. Restaurar: `scripts/restore.sh <archivo>` detiene `app`, hace un dump de seguridad, corre `pg_restore --clean --if-exists` y arranca `app`. Arranca solo con el stack, sin Programador de tareas de Windows.
11. **Arranque automático**: Docker Desktop con "Start Docker Desktop when you sign in" y los contenedores con `unless-stopped`. Limitación aceptada: Docker Desktop arranca al **iniciar sesión** y no al encender el PC, así que después de un reinicio (p. ej. Windows Update) el juego no está hasta que alguien entra a la sesión (decisión U7).
12. **Fin de línea**: `.gitattributes` con `mvnw` y `*.sh` en `eol=lf`. Con `core.autocrlf=true` hoy `mvnw` queda en CRLF en el disco y `sh mvnw` falla dentro del contenedor (lo mismo pasaría con `backup.sh` montado).
13. **Operación**: runbook en `docs/deploy/runbook.md` (borrador en el Anexo E). Versiones = imagen `shipbender:${APP_TAG}` con el sha corto de git. Rollback = volver al `APP_TAG` anterior (la imagen sigue en local). Si la versión nueva migró el esquema, hay que restaurar antes el backup previo a la actualización (Flyway solo avanza). Se despliega desde un clon limpio en `main`, nunca desde la copia de trabajo de desarrollo.

## Actualización 2026-11-22 (tarea 610)
En el servidor real (Debian, Caddy + túnel existente) `app` publica solo en `127.0.0.1:APP_PORT` y `tunnel` pasa a ser opcional (`profiles: ["tunnel"]`); Caddy debe fijar `X-Forwarded-Proto: https`. Reemplaza el "Ningún servicio tiene `ports:`" del punto 3 y el `TUNNEL_TOKEN` obligatorio del punto 4 (ahora vacío por defecto: Compose interpola también los perfiles inactivos). Ver `docs/deploy/runbook.md`.

## Actualización 2026-11-22 (respuesta del administrador del servidor; sustituye lo anterior donde choque)
El servidor (Debian 13) impone invariantes: ninguna clave remota tiene Docker ni sudo (el grupo `docker` = root), la app publica solo en `127.0.0.1:3100`, redes por app con subred fija (`10.240.160.0/24`) y sin salida, secretos generados en el servidor (`secrets.env`, root 600), contenedores endurecidos (`cap_drop: ALL`, no-new-privileges, no root, `mem_limit`+`memswap_limit`+`pids_limit`, tags fijos), todo en el backup del servidor. **Decisión del usuario: adoptar su diseño.**
- **Entrega**: paquete por SSH (`docker save | gzip` + `compose.yaml` + `deploy/servidor.env`) al comando `recibir` de `git-shell` (usuario `deploy`, sin docker ni sudo, `from=192.168.0.221`); un runner root (`server-app-deploy`, basado en nuestro `deploy.sh`) valida el compose, le suma su override (puerto, subred, límites), hace backup, `up -d --no-build`, espera la salud (`SALUD=/`, sin actuator) y vuelve a la imagen anterior si falla (conserva 3). Un compose que cambie respecto del aprobado pide `sudo server-app-aprobar shipbender`.
- **Carpetas** `/opt/compose/apps/{conf,compose,data}/shipbender` (SSD, en el backup). Proyecto `app-shipbender`, contenedores `app-shipbender` y `app-shipbender-db`. Postgres propio en red `internal` (no el compartido).
- **Backups**: `server-backup` (03:00, dump + prueba de restauración, HDD `/srv/data`, 7 días) + dump previo a cada deploy; el servicio `backup` y `scripts/backup.sh`/`restore.sh` salen del proyecto. Copia externa: más adelante (R2/B2 común); hoy solo la copia manual cifrada mensual del admin.
- **Caddy** (lo genera `server-app-public`): `reverse_proxy 127.0.0.1:3100` con `X-Forwarded-Proto https` y XFF desde `Cf-Connecting-IP`. Se mantiene `forward-headers-strategy=native` (punto 6). Cada `caddy reload` corta WebSockets abiertos (se propone `stream_close_delay 5m`); Cloudflare corta ociosos a ~100 s: heartbeats STOMP + reconexión (ya existen).
- **Cloudflare**: ruta en el panel (admin), Access Bypass, SSL Full (strict), rate limit 10 req/10 s por IP en `POST /api/auth/login` y `/api/players` (la crea el admin; falta; la app no tiene limitador propio).
- **Recursos**: app 768m, db 256m (`shared_buffers=64MB`, `max_connections=20`), JVM `MaxRAMPercentage=70` + SerialGC, Hikari 8. Quedan ~1,7 GiB libres.
- **Flyway**: migraciones compatibles hacia atrás; el rollback solo vuelve la imagen (si la migración rompe: restaurar el dump previo).
- Tarea 620 implementa; 621 la revisa. Estado y guion en `docs/deploy/PRIMER-DESPLIEGUE.md`.

## Alternativas descartadas
- **Un solo stage con `-Pfrontend`** (el plugin baja Node): funciona, pero cualquier cambio en Java reinstala Node y repite `npm ci` sin caché. **Jar por capas (`-Djarmode=tools extract`)**: sirve para subir imágenes a un registry; acá se construye y se ejecuta en la misma máquina.
- **Imagen JRE Ubuntu/distroless**: no trae `wget`, que el healthcheck necesita. **`maven:3.9-temurin-21`** en lugar de `mvnw`: desacopla la versión de Maven de la del wrapper.
- **Actuator `/actuator/health`**: ver punto 2. **Healthcheck con `curl` a `/api/...`**: necesita token.
- **Publicar `8080`/`5432` en `127.0.0.1`** para depurar: hoy no hace falta (`docker compose exec` alcanza) y choca con el rango 4700-4759 de desarrollo.
- **`forward-headers-strategy=framework`** (`ForwardedHeaderFilter`): confía en la cabecera venga de donde venga; `native` filtra por IP del proxy. **`allowedOriginPatterns("*")`**: elimina la defensa contra *cross-site WebSocket hijacking*.
- **Túnel con `config.yml` + credenciales JSON**: dos archivos secretos más; con el token, la ruta queda en el panel. **Cloudflare Access delante del juego**: el juego es público (decisión U3).
- **Backup con cron/imagen de terceros** (`postgres-backup-local`): una imagen ajena más con la contraseña de la BD. **Programador de tareas + `docker compose exec`**: un paso manual más en Windows y no viaja con el compose. **Volumen bind para `pgdata` en NTFS**: lento y con problemas de permisos en Docker Desktop.
- **Docker Engine en WSL2 con systemd como servicio**: arranca sin iniciar sesión, pero es una instalación aparte de Docker Desktop, que además se comparte con los tests.

## Consecuencias
- El primer arranque crea el esquema con Flyway sobre una BD vacía. `docker compose down -v` o `docker volume rm shipbender_pgdata` **borran todos los datos** (se avisa en el runbook).
- Prod y tests comparten Docker Desktop: un `./mvnw test` durante el juego consume RAM de la misma VM de WSL2. No chocan puertos (prod no publica ninguno).
- Para escalar a más de una instancia hace falta un broker externo (ADR 001): fuera de alcance.

## Decisiones tomadas por el usuario (2026-11-21)
- **U1** hostname: `shipbender.racnar.dev`. **U5** expiración del JWT en prod: 480 min (8 h). **U4** backups en otro disco físico o carpeta sincronizada, fuera del repo (ruta: la da el usuario en 607).
- **U2** servidor: **OTRA MÁQUINA** (no esta). Consecuencias: (a) las tareas 601-606 se desarrollan y prueban aquí con `COMPOSE_PROJECT_NAME=shipbender-test` (el compose es independiente del host); (b) la 607 y los pasos manuales se adaptan al sistema operativo de esa máquina, **que el usuario aún no ha indicado** (pendiente: SO, Docker/Compose instalados, RAM, disco para backups); (c) el repo se clona allí (rama `main`), no se usa `T:\Shipbender`; (d) el arranque automático tras reinicio depende de ese SO (en Linux, `systemctl enable docker` + `restart: unless-stopped`; en Windows, el paso de Docker Desktop del ADR).
- **Aplican las recomendaciones del arquitecto** salvo aviso en contra: U3 sin Cloudflare Access (rate limiting en login/registro), U6 registro abierto, U7 arranque con el sistema según el SO.

## Decisiones propuestas originalmente (recomendación entre paréntesis)
U1 hostname público (`shipbender.racnar.dev`: deja el apex libre) · U2 servidor (esta misma máquina, pero desde un clon aparte, p. ej. `C:\srv\shipbender`, en `main`) · U3 Cloudflare Access (no: juego público; en su lugar una regla de rate limiting en Cloudflare para `POST /api/auth/login` y `POST /api/players`, el plan Free permite 1) · U4 destino de backups (`BACKUP_DIR` en otro disco físico o en una carpeta sincronizada con la nube, p. ej. OneDrive; no dentro del clon) · U5 expiración del JWT (480 min) · U6 registro abierto en prod (sí) · U7 arranque tras reinicio (Docker Desktop al iniciar sesión + no suspender. Inicio de sesión automático solo si el PC está físicamente seguro; si no, se acepta la caída hasta que alguien entre).

## Pasos manuales del usuario (en orden)
1. Tomar U1-U7. 2. En el servidor: clonar el repo en `C:\srv\shipbender` (rama de F6 o `main`); en Docker Desktop activar *Start Docker Desktop when you sign in* y comprobar *Resources* ≥ 4 GB; en Windows, *Energía* → no suspender con corriente. 3. Cloudflare Zero Trust (si lo pide, elegir el plan Free) → *Networks → Tunnels → Create a tunnel* → *Cloudflared* → nombre `shipbender` → copiar el **token** (no ejecutar el comando de instalación que ofrece). 4. En el túnel → *Public Hostname* → subdominio `shipbender`, dominio `racnar.dev`, tipo `HTTP`, URL `app:8080`, sin *HTTP Host Header* → guardar (crea el CNAME). 5. En el dashboard de `racnar.dev`: *SSL/TLS → Edge Certificates → Always Use HTTPS* activado; *Network → WebSockets* activado (viene activado). 6. (Opcional, U3) *Security → WAF → Rate limiting rules*: 1 regla para `POST` a `/api/auth/login` y `/api/players`. 7. Copiar `.env.example` → `.env` y completarlo: `openssl rand -base64 48` (Git Bash) para `JWT_SECRET` y `POSTGRES_PASSWORD`, el token del paso 3 y `BACKUP_DIR`. 8. `docker compose up -d --build` y seguir la tarea 607. 9. Reiniciar Windows una vez, iniciar sesión y comprobar que el juego vuelve solo.

## Notas de aprendizaje
1. `application.properties:23` `spring.profiles.default=dev`: falla abierta. Si en prod falta el perfil, arranca el seeder (usuario ADMIN `marco@admin.com` / `123`) con un secreto JWT conocido (`application-dev.properties:1`). Alternativa: que lo inseguro sea lo que hay que pedir explícitamente (`-Dspring-boot.run.profiles=dev` en el arranque de dev) o que el seeder exija además una variable. Principio: *secure by default / fail-safe defaults*. En F6 se mitiga con `ENV SPRING_PROFILES_ACTIVE=prod` en la imagen.
2. `WebSocketConfig.java:34`: la regla de mismo origen es implícita y depende del esquema que ve el servidor, que detrás de un proxy que termina TLS es otro. Principio: hacer explícita la frontera de confianza (proxies de confianza → `RemoteIpValve`).
3. `WebSecurityConfig.java:34`: `connect-src ws: wss:` anula la CSP como defensa contra la exfiltración. Principio: mínimo privilegio en las políticas.
4. `JwtService.java:38-41`: `LocalDateTime` + `ZoneId.systemDefault()` para calcular un instante; `Instant.now().plus(Duration.ofMinutes(n))` no depende de la zona horaria del host (en el contenedor es UTC, así que no rompe nada hoy).

---

## Anexo A · `Dockerfile`
```dockerfile
# syntax=docker/dockerfile:1
FROM node:22-slim AS front
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
COPY --from=front /fe/dist src/main/resources/static
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -DskipTests package \
 && cp target/shipbender-*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
COPY --from=build /app.jar /app/app.jar
USER app
# si falta el perfil, Spring usa dev (seeder + secreto conocido): prod fijo en la imagen
ENV SPRING_PROFILES_ACTIVE=prod PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
```
`.dockerignore`: `.git`, `.idea`, `.claude`, `.env`, `.env.*`, `target`, `docs`, `backups`, `frontend/node_modules`, `frontend/node`, `frontend/dist`, `frontend/test-results`, `frontend/playwright-report`, `**/*.log`.
`.gitattributes`: `* text=auto` · `mvnw text eol=lf` · `*.sh text eol=lf` · `*.cmd text eol=crlf`.

## Anexo B · `docker-compose.yml`
```yaml
name: shipbender

x-logging: &logging
  driver: json-file
  options: { max-size: "10m", max-file: "3" }

x-db-env: &db-env
  POSTGRES_DB: ${POSTGRES_DB:-shipbender}
  POSTGRES_USER: ${POSTGRES_USER:-shipbender}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?falta POSTGRES_PASSWORD en .env}

services:
  db:
    image: postgres:17-alpine
    restart: unless-stopped
    environment: *db-env
    volumes: [pgdata:/var/lib/postgresql/data]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks: [backend]
    mem_limit: 512m
    logging: *logging

  app:
    build: .
    image: shipbender:${APP_TAG:-latest}
    restart: unless-stopped
    depends_on: { db: { condition: service_healthy } }
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${POSTGRES_DB:-shipbender}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-shipbender}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:?falta POSTGRES_PASSWORD en .env}
      JWT_SECRET: ${JWT_SECRET:?falta JWT_SECRET en .env}
      JWT_EXPIRATION_MINUTES: ${JWT_EXPIRATION_MINUTES:-480}
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/"]
      interval: 30s
      timeout: 5s
      start_period: 60s
      retries: 3
    networks: [backend, edge]
    mem_limit: 768m
    logging: *logging

  tunnel:
    image: cloudflare/cloudflared:<VERSION>   # fijar la última estable al implementar (tarea 603)
    command: tunnel --no-autoupdate run
    restart: unless-stopped
    environment:
      TUNNEL_TOKEN: ${TUNNEL_TOKEN:?falta TUNNEL_TOKEN en .env}
    depends_on: { app: { condition: service_healthy } }
    networks: [edge]
    mem_limit: 128m
    logging: *logging

  backup:
    image: postgres:17-alpine
    restart: unless-stopped
    depends_on: { db: { condition: service_healthy } }
    environment:
      PGHOST: db
      PGDATABASE: ${POSTGRES_DB:-shipbender}
      PGUSER: ${POSTGRES_USER:-shipbender}
      PGPASSWORD: ${POSTGRES_PASSWORD:?falta POSTGRES_PASSWORD en .env}
      RETENTION_DAYS: ${RETENTION_DAYS:-14}
    volumes:
      - ${BACKUP_DIR:-./backups}:/backups
      - ./scripts:/scripts:ro
    entrypoint: ["sh", "-c", "while true; do sh /scripts/backup.sh; sleep 86400; done"]
    networks: [backend]
    mem_limit: 128m
    logging: *logging

networks:
  backend: { internal: true }
  edge: {}

volumes:
  pgdata:
```

## Anexo C · `.env.example` y cambios de backend (tarea 602)
```dotenv
# Copiar a .env (NO versionado). Secretos: openssl rand -base64 48 (Git Bash)
APP_TAG=latest
POSTGRES_DB=shipbender
POSTGRES_USER=shipbender
POSTGRES_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=480
TUNNEL_TOKEN=
BACKUP_DIR=./backups
RETENTION_DAYS=14
```
Backend: `application.properties` + `server.forward-headers-strategy=native`; `WebSecurityConfig.java:34` `connect-src 'self' ws: wss:` → `connect-src 'self'`.

## Anexo D · scripts
`scripts/backup.sh` (POSIX sh, corre DENTRO del servicio `backup`):
```sh
#!/bin/sh
# Un dump de la BD en /backups y borra los de más de RETENTION_DAYS días.
set -eu
f="/backups/shipbender-$(date +%Y%m%d-%H%M%S).dump"
pg_dump -Fc -f "$f.tmp"
mv "$f.tmp" "$f"   # el .tmp evita dejar un dump a medias con nombre válido
find /backups -name 'shipbender-*.dump' -mtime +"${RETENTION_DAYS:-14}" -delete
echo "backup ok: $f"
```
`scripts/restore.sh` (bash en el host, Git Bash):
```bash
#!/usr/bin/env bash
# Uso: scripts/restore.sh shipbender-AAAAMMDD-HHMMSS.dump (archivo dentro de BACKUP_DIR). REEMPLAZA los datos actuales.
set -euo pipefail
export MSYS_NO_PATHCONV=1   # Git Bash convierte "/backups/..." en rutas de Windows al llamar a docker.exe
cd "$(dirname "$0")/.."
file="${1:?uso: scripts/restore.sh <archivo.dump>}"
docker compose exec -T backup test -f "/backups/$file"
read -r -p "Se reemplaza la BD por $file. Escribe 'si' para seguir: " ok
[ "$ok" = "si" ] || exit 1
docker compose stop app
docker compose exec -T backup sh /scripts/backup.sh          # copia de lo actual, por si acaso
docker compose exec -T backup sh -c 'pg_restore --clean --if-exists --no-owner -d "$PGDATABASE" "/backups/$1"' _ "$file"
docker compose start app
```

## Anexo E · Runbook (borrador; la tarea 605 lo deja en `docs/deploy/runbook.md` con los comandos verificados)
Todo desde el clon de prod en Git Bash, con `export MSYS_NO_PATHCONV=1`.
- **Estado / logs**: `docker compose ps` · `docker compose logs -f --tail 200 app` (también `tunnel`, `backup`, `db`).
- **Actualizar**: `git pull` → `docker compose exec backup sh /scripts/backup.sh` → en `.env`, `APP_TAG=$(git rev-parse --short HEAD)` → `docker compose up -d --build app` → `docker compose ps` (app `healthy`) y probar el login en el dominio.
- **Rollback**: `APP_TAG` anterior en `.env` (`docker image ls shipbender`) → `docker compose up -d app`. Si la versión nueva traía una migración `V*__` nueva: antes, `scripts/restore.sh <dump previo a la actualización>`.
- **Backup a mano**: `docker compose exec backup sh /scripts/backup.sh`. **Restaurar**: `scripts/restore.sh <archivo>`. En una máquina nueva: `docker compose up -d db backup`, copiar el dump a `BACKUP_DIR`, restaurar y `docker compose up -d`.
- **Rotar `JWT_SECRET`** (cierra todas las sesiones): editar `.env` → `docker compose up -d app`. **Rotar el token del túnel**: en el panel → `.env` → `docker compose up -d tunnel`.
- **Nunca**: `docker compose down -v`, `docker volume prune` ni `docker system prune --volumes` (borran `pgdata`).
