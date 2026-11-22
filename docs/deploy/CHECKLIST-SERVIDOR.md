# Checklist de despliegue — Shipbender

Flujo vigente: `scripts/ship.sh` (PC de desarrollo) → `ssh tva-deploy recibir shipbender <sha>` → runner del administrador (valida, `up -d --no-build`, espera salud, rollback). Detalle: `docs/deploy/runbook.md`, `docs/deploy/PRIMER-DESPLIEGUE.md`, `docs/adr/007-deploy.md`.

## A. Antes del primer despliegue (lo hace el administrador)
- [ ] Usuario de envío (git-shell, sin docker ni sudo) con clave restringida a la IP de la PC (`from=`, ADR 007).
- [ ] `APP_PORT` asignado con `server-app-public` (publicado solo en `127.0.0.1:<APP_PORT>:8080`).
- [ ] Caddy: `reverse_proxy 127.0.0.1:<APP_PORT> { header_up X-Forwarded-Proto https }` (sin esto el WebSocket da 403).
- [ ] Imagen `postgres:17.11-alpine` precargada en Docker (el servidor no hace pull).
- [ ] `data/pgdata` con dueño `70:70` y modo `0700`.
- [ ] Secretos `JWT_SECRET` y `POSTGRES_PASSWORD` generados en el servidor; nunca en el repo ni en un chat.
- [ ] Red `web` e `interna` (interna sin salida) según el paquete; sin `ports` ni nombres propios en el compose del repo.

## B. Cloudflare (lo hace el usuario en el panel)
- [ ] Ruta del hostname `shipbender.racnar.dev` hacia Caddy.
- [ ] Access: política **Bypass** para `shipbender.racnar.dev` (el juego es público).
- [ ] SSL/TLS: *Always Use HTTPS* activado; *Network → WebSockets* activado.
- [ ] Regla de rate limiting: 10 peticiones / 10 s por IP sobre `POST /api/auth/login` y `POST /api/players`.

## C. Desplegar
- [ ] En `main`, árbol limpio, `./mvnw test` en verde.
- [ ] `scripts/ship.sh --dry-run` sin errores; después `scripts/ship.sh`.
- [ ] Código de salida 0. Si es 3, el administrador aprueba el compose y se reenvía; si es 1, el servidor ya volvió a la versión anterior.

## D. Comprobar tras el despliegue
- [ ] `app` y `db` en `healthy` (con el comando que indique el administrador).
- [ ] `curl -sI https://shipbender.racnar.dev/` → 200 de la app, **no** 302 a `cloudflareaccess.com`.
- [ ] Handshake WebSocket con `Origin: https://shipbender.racnar.dev` → 101; con origen ajeno → 403.
- [ ] Registrar dos jugadores en dos navegadores, jugar un turno, enviar un emote.
- [ ] `/swagger` devuelve 404 (perfil prod).
- [ ] Copia de seguridad hecha y probada por el administrador antes de dar el juego por bueno.

## E. Reinicio del servidor
- [ ] Reinicio completo fuera de las ventanas de mantenimiento (03:00 y 04:30).
- [ ] En ≤ 3 min el dominio responde 200 sin intervención; `db` y `app` vuelven `healthy`.

## F. Qué no hacer
- [ ] No `docker compose down -v`, `docker volume prune`, `docker system prune --volumes` ni `docker image prune -a`.
- [ ] No editar el compose en el servidor a mano: un cambio pasa por el paquete y la aprobación.
