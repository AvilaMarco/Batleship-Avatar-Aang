# 610 · devops · Compose adaptado al servidor real (Caddy + puerto en loopback, túnel opcional)
Estado: pendiente
Fase: F7
Depende de: 609
Paralelizable: no (toca docker-compose.yml, .env.example y el runbook)
Origen: usuario, 2026-11-22: diseño del servidor (Debian 13): `cloudflared` va en Docker en la red `tunel` y solo apunta al listener público de Caddy (10.240.140.1:8080); Caddy corre en la red del host y reparte a cada app con `reverse_proxy 127.0.0.1:<puerto>`; cada app publica SOLO en `127.0.0.1:<puerto>:8080`; el servidor tiene una sola salida a Internet (el túnel existente) y NO debe levantarse un túnel propio; el administrador del servidor asigna el puerto.
Riesgo: alto (cambia la exposición de red de la app)

Objetivo: el mismo `docker-compose.yml` sirve en el servidor (sin túnel, app en loopback) y en otras máquinas (con túnel propio opcional).

Archivos: `docker-compose.yml`, `.env.example`, `docs/deploy/runbook.md`, `docs/adr/007-deploy.md` (anotar la decisión en una línea).

Diseño (cerrado):
- `tunnel`: añadir `profiles: ["tunnel"]` (desactivado por defecto; se activa con `docker compose --profile tunnel up -d`). Quitar la dependencia de `TUNNEL_TOKEN` del `config` por defecto: con el perfil desactivado, `docker compose config` NO debe exigir `TUNNEL_TOKEN`.
- `app`: añadir `ports: ["127.0.0.1:${APP_PORT:?falta APP_PORT en .env (el administrador del servidor lo asigna)}:8080"]`. Solo loopback; nunca `0.0.0.0`. `db` y `backup` no publican nada. La red `edge` se mantiene (la usa el perfil `tunnel`).
- `.env.example`: `APP_PORT=` (sin valor, con comentario "asignado por el administrador del servidor; no usar el rango 4700-4759, que es de desarrollo") y `TUNNEL_TOKEN=` marcado como "solo con `--profile tunnel`".
- Runbook: sección nueva "Publicación detrás de Caddy" con: el flujo (Cloudflare → cloudflared → Caddy → 127.0.0.1:APP_PORT), qué debe hacer el administrador (alta del hostname `shipbender.racnar.dev` con su herramienta y la ruta de Cloudflare; **Cloudflare Access: el juego es público, así que `shipbender.racnar.dev` necesita una política Bypass si `*.racnar.dev` está cerrado por defecto**), y la comprobación del WebSocket (ver abajo). Mover la creación del túnel propio a una sección "Túnel propio (opcional, solo otras máquinas)".
- Cabecera crítica detrás de Caddy: Tomcat aplica la regla de mismo origen del WebSocket con `server.forward-headers-strategy=native`, que necesita que llegue `X-Forwarded-Proto: https` y el `Host` original. Caddy termina la conexión HTTP de cloudflared; si no conserva o fija esa cabecera, el handshake daría 403. El runbook debe pedir al administrador que Caddy preserve `Host` (por defecto) y envíe `X-Forwarded-Proto: https` para este hostname (por ejemplo `header_up X-Forwarded-Proto https` en el `reverse_proxy` del sitio), y documentar cómo verificarlo con curl (handshake con `Origin: https://shipbender.racnar.dev` y esa cabecera → 101; con `Origin` ajeno → 403).

Criterios de aceptación (proyecto aislado `COMPOSE_PROJECT_NAME=shipbender-test`, `MSYS_NO_PATHCONV=1`, `.env` de prueba no versionado con APP_PORT=4756, BACKUP_DIR temporal fuera del repo):
- `docker compose config -q` sin `TUNNEL_TOKEN` → OK; sin `APP_PORT` → falla con el mensaje.
- `docker compose config | grep -A2 ports:` muestra SOLO `127.0.0.1:4756:8080` para `app` y ningún otro `ports:`; `docker compose --profile tunnel config -q` (con TUNNEL_TOKEN de prueba) → OK y lista `tunnel`.
- `docker compose up -d db app backup`: `db` y `app` `healthy`; `curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:4756/` → 200; desde otra interfaz del host (la IP LAN de la máquina, si existe) el puerto NO responde (`netstat`/`curl` a esa IP:4756 falla).
- Handshake como lo enviaría Caddy: `curl -s -o /dev/null -w '%{http_code}' -H 'Host: shipbender.racnar.dev' -H 'Origin: https://shipbender.racnar.dev' -H 'X-Forwarded-Proto: https' -H 'Connection: Upgrade' -H 'Upgrade: websocket' -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==' http://127.0.0.1:4756/the-last-shipbender` → 101; con `Origin: https://evil.example` → 403; con `X-Forwarded-Proto: http` y Origin https → 403 (esto documenta lo que pasa si Caddy no fija la cabecera).
- Limpieza como en 603 (`down -v` solo del proyecto de prueba con echo previo, borrar `.env` y carpeta temporal).
- Commit `build: compose con puerto en loopback y túnel opcional para el servidor real (tarea 610)`.

## Resultado
