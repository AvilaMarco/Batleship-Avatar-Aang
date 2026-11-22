# Respuesta de Shipbender al administrador del servidor (pegar tal cual)

Gracias, muy completo. Adoptamos tu diseño (paquete por SSH → `recibir` → runner root con validador y override). Aquí van mis datos y decisiones; abajo, lo que necesito que me confirmes antes de que adapte los scripts. Sigue siendo solo consulta: no cambies nada todavía.

## Datos que pedías
- **Variables** (nombres; valores los generas tú en `secrets.env`): `JWT_SECRET` (≥ 32 caracteres; `openssl rand -base64 48`), `POSTGRES_PASSWORD`. No secretas: `JWT_EXPIRATION_MINUTES=480`, `POSTGRES_DB=shipbender`, `POSTGRES_USER=shipbender`. La app lee `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` (las compone mi compose a partir de las anteriores). Escucha en 8080 (`PORT`/`NODE_ENV` sobran y se ignoran). Perfil `prod` fijo en la imagen.
- **Ruta de salud**: `SALUD=/` (200 con el `index.html` público; barata y sin detalles). No añado actuator (una dependencia y una ruta más). Healthcheck del contenedor: `wget` (ya está en la imagen).
- **Salida a Internet**: ninguna. Sin correo, OAuth ni llamadas a terceros. La app solo habla con su Postgres. Redes sin salida, como propones.
- **Datos personales**: nick, email y hash bcrypt de la contraseña; partidas y estadísticas. Sin telemetría. IP real solo en logs (journald). Backups con esos hashes: root 600.
- **Rate limit** (Cloudflare): 10 peticiones / 10 s por IP, una sola regla con tu expresión (POST + host + `/api/auth/login`, `/api/players`). Límite en la propia app: lo pido como mejora aparte (hoy no lo tiene).
- **Acceso**: `from="192.168.0.221"` solo desde casa. Reuso la clave `pc-deploy` si es la misma para todos; si prefieres una aparte, te mando la pública.
- **Backup externo**: por ahora solo tu `server-backup` (+ tu copia manual mensual); R2/B2 común más adelante. No instales `age`.
- **Compose**: quito `edge`, el servicio `backup`, el `ports:` y el servicio `tunnel`; tú pones puerto 3100, subred 10.240.160.0/24 y límites. Pondré yo además `cap_drop: [ALL]`, `no-new-privileges`, `pids_limit`, `read_only: true` + `tmpfs: /tmp`, usuario no root (ya lo es), `mem_limit`/`memswap_limit` (app 768m, db 256m), `JAVA_TOOL_OPTIONS` con `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k`, Hikari 8, Postgres con `shared_buffers=64MB` y `max_connections=20`. Tags fijos (sha para la app; `postgres:17.x` exacto).
- **Forwarded headers**: dejo `server.forward-headers-strategy=native` (no `framework`: `native` solo confía en proxies de IP privada y tu Caddy reescribe las cabeceras). Verificaré con la cadena real que el WebSocket da 101.
- **Heartbeat/reconexión STOMP**: el front ya reconecta; confirmaré los heartbeats en la prueba.
- **Migraciones Flyway**: las siguientes serán compatibles hacia atrás (expand/contract); el rollback de una con migración rota = restaurar el dump previo a mano.

## Lo que necesito que me confirmes (solo consulta)
1. **Interfaz exacta de `recibir`**: argumentos, formato del paquete (¿un `tar` por stdin con `imagen.tar.gz`, `compose.yaml`, `deploy/servidor.env`?), tamaños máximos, códigos de salida y cómo veo el log en vivo. ¿Ruta del repo donde dejas `git-shell-commands/recibir`, para leerlo?
2. **Manifiesto `deploy/servidor.env`**: lista completa de campos (propongo: `PUERTO_APP=8080`, `SALUD=/`, `MEMORIA`, `USA_POSTGRES=1`, `SECRETOS=JWT_SECRET,POSTGRES_PASSWORD`, `ENV_FIJO=…`), y qué valida el servidor.
3. **Label de backup**: formato exacto (`tva.backup: pg_dump` en el servicio `db`) y cómo se pasan usuario/BD/contraseña al dump; cómo lo restauras tú (dime el comando) y si los dumps se pueden ver en `/srv/data/backups/server-maintenance/`.
4. **Postgres**: ¿lo permite la validación con volumen bajo `data/` (p. ej. `./data/pgdata:/var/lib/postgresql/data` con el uid del contenedor, 70 en Alpine)? ¿Y `tmpfs` y `read_only` en el compose de la app?
5. **Imágenes**: la de Postgres también la envío en el paquete (`docker save`); ¿o puedes bajarla tú desde Docker Hub (el host sí sale)? Prefiero que la bajes tú: paquete de ~130 MB en vez de ~170.
6. **Healthcheck de Docker** en el compose: `depends_on: service_healthy` (tras el `up`) — ¿lo conservas o lo quitas en el compose aprobado?
7. **Primer despliegue**: orden exacto entre `server-app-new shipbender docker`, `server-app-public`, `server-app-kuma`, la ruta de Cloudflare y mi primer paquete, y el alta de `server-firewall-start` para la subred 10.240.160.0/24. Si algo falla tras la ruta (app caída), ¿qué ve el visitante?
8. **Aprobación**: el primer compose pasa por `server-app-aprobar`; ¿cuánto tarda y cómo me avisas?
9. **Puerto 3100** y proyecto `app-shipbender`: confirmo. ¿Prefieres que mi compose use `name: app-shipbender` o lo fuerzas tú?
10. Mi `deploy.sh` actual (≈200 líneas, sin secretos) está en el repo como base del runner; te lo paso cuando lo pidas (por ruta de archivo, sin pegarlo aquí).

Dime también si algo de lo anterior contradice una invariante tuya.
