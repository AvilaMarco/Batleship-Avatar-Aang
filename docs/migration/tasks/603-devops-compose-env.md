# 603 · devops · docker-compose.yml y .env.example (sin túnel real)
Estado: pendiente
Fase: F6
Depende de: 601, 602
Paralelizable: no (necesita la imagen con 602). Usa la Docker Desktop compartida: no correr a la vez que `./mvnw test`.
Origen: ADR 007 puntos 3, 4, 8 y 9; pedido del usuario "docker-compose.yml (app + postgres:17-alpine + cloudflared; red interna; la app y la BD SIN puertos publicados…)".
Riesgo: alto (secretos y red expuesta a Internet) → revisión en 606

Objetivo: el stack arranca con un `.env`, sin publicar puertos, con el perfil prod, la BD vacía y sin usuarios por defecto.

Archivos: `docker-compose.yml`, `.env.example`.

Diseño (cerrado): ADR 007 Anexo B y Anexo C (`.env.example`), tal cual, más el servicio `backup` (sin `scripts/backup.sh` todavía el servicio fallará: verificar solo `db` y `app` en esta tarea). Fijar `cloudflare/cloudflared:<VERSION>` a la última etiqueta estable de Docker Hub (no `latest`).

Verificación con un proyecto de prueba aislado para no tocar el de prod. Todo en Git Bash con `export MSYS_NO_PATHCONV=1` y `export COMPOSE_PROJECT_NAME=shipbender-test`. `.env` de prueba con valores generados al momento (`openssl rand -base64 48`), `TUNNEL_TOKEN=dummy`. Nunca commitear ese `.env`.

Criterios de aceptación:
- `docker compose config --quiet` → sin errores; `docker compose config | grep -E '^\s+ports:'` → sin salida.
- Sin `.env` (renombrarlo un momento), `docker compose config` falla con `falta JWT_SECRET en .env` (o la primera variable que falte).
- `docker compose up -d --build db app` → `docker compose ps` muestra `db` y `app` `healthy` en menos de 2 min.
- `docker compose logs app | grep -i 'profile is active: "prod"'` → 1 línea; sin `DevDataSeeder`.
- `docker compose exec db psql -U shipbender -d shipbender -tc 'select count(*) from player'` → `0`.
- Desde la red `edge`: `docker run --rm --network shipbender-test_edge curlimages/curl -s -o /dev/null -w '%{http_code}' http://app:8080/swagger` → `404`; lo mismo con `/docs` → `404` (o 401/403, pero nunca 200).
- Handshake como lo haría cloudflared: `docker run --rm --network shipbender-test_edge curlimages/curl -s -o /dev/null -w '%{http_code}' -H 'Host: shipbender.racnar.dev' -H 'Origin: https://shipbender.racnar.dev' -H 'X-Forwarded-Proto: https' -H 'Connection: Upgrade' -H 'Upgrade: websocket' -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==' http://app:8080/the-last-shipbender` → `101`; con `Origin: https://evil.example` → `403`.
- `db` sin salida a Internet: `docker compose exec db wget -q -T 5 -O- https://example.com` falla.
- Limpieza: `docker compose down -v` **solo con `COMPOSE_PROJECT_NAME=shipbender-test`** (comprobarlo con `echo` antes) y borrar el `.env` de prueba.
- Commit `build: docker compose de producción y .env.example (tarea 603)`.

## Resultado
