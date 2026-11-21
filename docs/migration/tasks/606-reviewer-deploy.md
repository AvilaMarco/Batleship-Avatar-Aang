# 606 · reviewer · Revisión de riesgo alto de F6 (secretos, red, proxy, backups)
Estado: hecha
Fase: F6
Depende de: 601, 602, 603, 604 (antes de exponer el dominio en 607)
Paralelizable: sí, con 605 (solo lee)
Origen: CLAUDE.md "Riesgo alto (seguridad...): tarea de `reviewer`"; tareas 602, 603, 604.
Riesgo: alto

Objetivo: revisar (solo lectura) y reportar hallazgos BLOQUEANTE / MENOR antes de poner el juego en Internet. No editar.

Archivos a revisar: `git log --oneline main..HEAD` y `git show` de 601-604; `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example`, `.gitignore`, `scripts/*.sh`, `application*.properties`, `WebSecurityConfig.java`, ADR 007.

Lista mínima:
- Ningún secreto versionado: `git log -p main..HEAD | grep -iE 'JWT_SECRET=.|PASSWORD=.|TUNNEL_TOKEN=.'` solo muestra claves vacías o de ejemplo; `.env` ignorado (`git check-ignore .env`); `.dockerignore` excluye `.env`, así que no entra en la imagen.
- Sin `ports:` en el compose; `db` y `backup` solo en la red `internal`; versiones de imagen fijadas (sin `latest`, salvo `shipbender:${APP_TAG}`).
- La imagen corre como no root, con `SPRING_PROFILES_ACTIVE=prod`, sin devtools y sin seeder.
- `forward-headers-strategy=native`: confirmar que no se puede falsificar `X-Forwarded-*` desde fuera (solo entra cloudflared) y que la regla de mismo origen sigue activa (test 2 de 602).
- CSP: `connect-src 'self'` sin `ws:`/`wss:` comodín.
- Backups: `restore.sh` pide confirmación, hace un dump de seguridad antes y valida el archivo antes de detener `app`; los dumps no quedan dentro del repo ni de la imagen.
- `JWT_EXPIRATION_MINUTES` por defecto 480; Swagger apagado en prod.

Criterio de aceptación: sección `## Resultado` con los hallazgos (archivo:línea, severidad, propuesta) y un veredicto. Si hay BLOQUEANTES, el central crea las tareas de arreglo antes de 607.

## Resultado

Revisor: rol `reviewer` (solo lectura; el central transcribió el informe).

**Veredicto: aprobado con condiciones. Sin bloqueantes de código/config.** Se puede exponer a Internet SI se hacen los pasos manuales de Cloudflare (ver 607) y se acepta el riesgo del punto 4.

MENOR
1. `application.properties:23` (`spring.profiles.default=dev`) falla abierto: un `SPRING_PROFILES_ACTIVE=` vacío (p. ej. `docker run -e`) arrancaría `DevDataSeeder` (admin con contraseña conocida). Con el compose no ocurre (la imagen fija `prod`, el compose no lo toca, y `jwt.secret` en prod no tiene valor por defecto). No se cambia el código (los tests y el arranque de dev dependen del default); mitigación: no usar `docker run` sueltos y revisar el perfil en el arranque (runbook).
2. `scripts/backup.sh`: sin `umask 077`; los dumps (emails, hashes bcrypt) salen 0644. → tarea 608.
3. `.env.example:10`: `BACKUP_DIR=./backups` dentro del clon contradice U4. → tarea 608.
4. Sin rate limiting en servidor; `RegisterRequest` acepta contraseñas de 1 carácter. La regla de Cloudflare (login y registro) pasa de opcional a **obligatoria** (607). Mejora de backlog: mínimo 8 caracteres (rompería los tests y el seed actuales que usan contraseñas cortas; hacerlo con qa).
5. `TUNNEL_TOKEN` visible con `docker inspect` y en `.env`: rotarlo en Zero Trust si se expone, no pegarlo en chats/logs, `.env` legible solo por el usuario. → runbook (608).
6. Imágenes por tag y no por digest: aceptable. (Tag `cloudflared:2026.11.1` verificado por el central en Docker Hub.)
7. `.dockerignore`: poner `**/.env*` por defensa. → 608.
8. Sin alertas si `backup.sh` falla (el bucle sigue). → runbook (mirar `docker compose logs backup`) (608).

OK (verificado por lectura): sin secretos en todo el historial de git; el secreto JWT antiguo solo sigue en el historial (hay que usar uno nuevo en prod; `JwtService` exige ≥32 caracteres); `.env` ignorado; compose sin `ports:`, `db` y `backup` en red `internal`; `app` sí tiene salida (red `edge`); `forward-headers-strategy=native` solo confía en IPs privadas y la regla de mismo origen sigue (test de 602); restore.sh sin inyección (comillas, rechaza `/` y `*`, valida antes de parar `app`, dump de seguridad, transacción única, `trap`); imagen no root sin devtools; CSP `connect-src 'self'`; Swagger apagado en prod (404); JWT 480 en el compose; `denyAll` por defecto. Con JWT por cabecera y no por cookie, el CSWSH pierde casi todo su impacto.

No verificado: arranque real, comportamiento con perfil vacío, X-Forwarded-Host en Tomcat, permisos de la carpeta de dumps en el servidor, panel de Cloudflare.

Pasos manuales en Cloudflare (todos obligatorios antes de dar la URL): un único Public Hostname `shipbender.racnar.dev` → `http://app:8080` (sin otros hostnames ni catch-all; no usar quick tunnels); no abrir puertos ni registros A hacia tu IP; SSL/TLS con Always Use HTTPS y modo Full (strict), WebSockets activado; **regla de rate limiting** para `POST /api/auth/login` y `POST /api/players`; comprobar que Bot Fight Mode/WAF no bloquea el WebSocket; desde fuera, `/swagger`, `/docs` y `/actuator` deben dar 404/403.
