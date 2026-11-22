# Mensaje para el administrador del servidor (pegar tal cual)

Hola. Voy a desplegar una app nueva en este servidor y necesito que **SOLO CONSULTES y respondas: no cambies nada** (ni archivos, ni servicios, ni Docker, ni Cloudflare). Todos los comandos propuestos son de lectura; si alguno no te sirve, dime cuál usaste. No pegues secretos (tokens, claves privadas, contenido de `.env`): basta con rutas, permisos y nombres.

**La app: Shipbender** — batalla naval 1 vs 1 por turnos con chat en tiempo real (WebSocket/STOMP). Un contenedor Spring Boot (Java 21, ~350 MB de RAM en reposo, límite 768 MB) + Postgres 17 + un contenedor de backups, con Docker Compose. Hostname: `shipbender.racnar.dev`, público (sin login de Cloudflare Access). Se publica solo en `127.0.0.1:<APP_PORT>:8080`; Caddy reparte. Sin túnel propio. El servidor no compila ni hace `docker pull`: desde mi PC envío la imagen por SSH (`docker save | gzip | ssh usuario@servidor 'docker load'`) con una clave restringida (`restrict,command="<ruta>/scripts/deploy.sh ssh-entry"`), y ese script corre `docker compose` (backup previo, `up --no-build`, espera `healthy`, rollback automático).

Respóndeme numerado. Donde diga **(confírmame)** es lo que ya me dijeron; di solo "correcto" o corrige. En cada ítem añade, si aplica: **cómo funciona aquí** y **cómo habría que adaptar** mi proyecto.

## A. Usuario de despliegue y SSH
1. ¿Existe (o puede crearse) un usuario solo para desplegar apps, **sin sudo y sin shell interactivo**, en el grupo `docker`? `id <usuario>; getent passwd <usuario>; getent group docker`
2. `sshd`: valores efectivos de `AllowUsers/AllowGroups`, `PasswordAuthentication`, `PermitUserEnvironment`, `AuthorizedKeysFile`, `Port`; ¿admite `restrict`, `command=` y `from=` en `authorized_keys`? `sudo sshd -T | grep -Ei 'allowusers|allowgroups|passwordauth|permituserenv|authorizedkeysfile|^port|authorizedkeyscommand'`
3. ¿Cómo llega mi PC al servidor: LAN, IP fija o hostname, puerto? ¿Tiene IP fija para poner `from="IP"`? ¿Y si hace falta VPN o túnel?
4. ¿Cómo se instala una clave pública (la mando yo, la pone el admin, otra vía)? ¿Existen `flock`, `gzip`, `bash` y `openssl`? `command -v flock gzip bash openssl`

## B. Carpetas y `.env`
5. Convención por app: carpeta raíz (p. ej. `/srv/apps/<app>`), propietario/grupo, permisos. Muéstrame cómo están `juntada` y `michido`: `ls -ld /srv/* /srv/*/* 2>/dev/null`
6. ¿Dónde va el `.env` de una app y con qué permisos (0600, dueño)? ¿Quién lo crea? Yo no tendré acceso: ¿cómo debo entregarte los secretos (JWT y contraseña de Postgres) sin pegarlos en un chat? ¿O los genera el propio usuario de despliegue con `openssl rand`?

## C. Puerto y alta de la app
7. ¿Cómo se asigna `APP_PORT`? ¿Rango, registro de puertos usados? `ss -tlnp | grep 127.0.0.1` Mi desarrollo usa 4700-4759: no debe usarse. Dime qué puerto me das.
8. Procedimiento exacto del alta con `server-app-public`: campos, manifiesto o archivo que debe aportar el proyecto, ejemplo con `juntada` (sin secretos) y qué cambia al ejecutarlo. `server-app-public --help`

## D. Caddy
9. Formato y ubicación de la configuración (Caddyfile, `conf.d`, JSON), cómo añade un sitio `server-app-public` y cómo se recarga. `systemctl status caddy | head -5; caddy version`, y el bloque de `juntada` (sin secretos).
10. **Crítico**: ¿el bloque de `shipbender.racnar.dev` puede incluir `reverse_proxy 127.0.0.1:<puerto> { header_up X-Forwarded-Proto https }`? Sin eso Spring rechaza el WebSocket con 403 (comprueba esquema y puerto del `Origin`). (confírmame) el listener público es `10.240.140.1:8080` y recibe HTTP de cloudflared.
11. Cabeceras por defecto hacia la app (`Host`, `X-Forwarded-For/Host/Proto`), ¿se confía en cloudflared (`trusted_proxies`)? ¿Hay timeouts o límites que corten un WebSocket largo (idle, `read_timeout`, `request_body max_size`)?
12. ¿Qué responde el 404 por defecto a un `Host` desconocido, y qué pasa con `shipbender.racnar.dev` mientras no esté dada de alta?

## E. Cloudflare
13. Procedimiento para la ruta del hostname hacia Caddy (quién la crea, dónde: panel Zero Trust, API, `config.yml`) y la política Access **Bypass** para `shipbender.racnar.dev` (confírmame que `*.racnar.dev` está cerrado por defecto y cada app tiene su Bypass). ¿Cómo compruebo que existe? (propongo `curl -sI https://<host>/` sin cookies: debe dar 200 de la app, no un 302 a `cloudflareaccess.com`)
14. ¿WebSockets activados en la zona y modo SSL/TLS (Full/Strict/Flexible)? ¿"Always Use HTTPS"?
15. ¿Quién crea la regla de rate limiting (WAF) para `POST /api/auth/login` y `POST /api/players` (plan Free: 1 regla)? ¿Ya hay una que comparta el cupo?

## F. Red de salida
16. Destinos permitidos hacia Internet por el firewall (¿Docker Hub? ¿apt? ¿otros?). Si hiciera falta un destino para backups (S3/R2/B2, otro host por SSH), ¿cómo se solicita y quién lo aprueba? Además: DNS (resolvers) y NTP (`timedatectl status`).

## G. Docker
17. `docker info` (resumen): versión, `Storage Driver`, `Logging Driver`, `Live Restore Enabled`, `Cgroup Version`, y si aparece algún aviso `No swap limit support` (necesito que `memswap_limit` funcione). `docker info 2>&1 | grep -Ei 'warning|storage driver|logging driver|live restore|cgroup'` y `cat /etc/docker/daemon.json`
18. Redes existentes y convención de nombres (`tunel`, otras): `docker network ls`. ¿Mis redes `backend` (interna) y `edge` por proyecto Compose chocan con algo o con rangos de IP usados? `docker network inspect $(docker network ls -q) --format '{{.Name}} {{range .IPAM.Config}}{{.Subnet}} {{end}}'`
19. Política de limpieza de imágenes (¿prune automático? Yo conservo las 2-3 últimas versiones y **no** debe correr `prune -a` ni `--volumes`: borrarían el rollback o los datos). `docker system df`
20. Memoria hoy: `docker stats --no-stream --format '{{.Name}} {{.MemUsage}}'` y `free -h`. ¿Cabe mi stack (~0,9 GB como máximo) con holgura, dado el swap en uso?

## H. Backups
21. Convención de las otras apps: carpeta, herramienta, frecuencia, retención. ¿Hay otro disco además de `/dev/sdb2`? `lsblk -f; df -h`
22. ¿Hay copias fuera del servidor (quién las hace y adónde)? Mis dumps (`pg_dump`, 0600) contienen hashes de contraseñas. Propongo: carpeta local `/srv/backups/shipbender` + copia cifrada (`age`/`gpg`) que mi PC trae por SSH, o un destino en frío. ¿Qué te parece y qué ya tienes montado?

## I. Arranque, monitorización y logs
23. Tras reinicio: orden Docker → Caddy → cloudflared, unidades systemd (`systemctl is-enabled docker caddy`), y cómo arrancan los contenedores de cloudflared y de las otras apps (`restart: unless-stopped`). ¿Se ha probado un reinicio completo?
24. Monitorización/alertas (uptime, disco, RAM): ¿qué hay y cómo se da de alta una app? ¿Dónde miro logs (`journalctl -u caddy`, logs de Docker)? ¿Qué ruta puede usar un chequeo de salud externo?

## J. Seguridad
25. Actualizaciones automáticas (`unattended-upgrades`), ¿reinician solos el servidor y cuándo? `systemctl is-enabled unattended-upgrades`
26. Firewall del host (`ufw status verbose` o `nft list ruleset`): ¿un puerto publicado por Docker en `127.0.0.1` queda fuera de sus reglas? ¿Hay `fail2ban` u otro endurecimiento SSH? ¿Qué prohíbe la política de la casa para una clave de despliegue que equivale a root por el grupo `docker`?

## K. Otros
27. Todo lo que creas que debo saber o adaptar (versión mínima de Compose, límites de recursos por app, cuotas de disco, ventanas de mantenimiento, convenciones de nombres de proyecto Compose, `userns-remap`, SELinux/AppArmor, etc.).

Al final, dame un **resumen en tabla**: ítem · estado (OK / falta / hay que adaptar) · qué debo cambiar yo y qué haces tú. Gracias.
