# 607 · central (con el usuario) · Puesta en marcha en el dominio y verificación
Estado: pendiente
Fase: F6
Depende de: 605, 606 (sin BLOQUEANTES abiertos) y los pasos manuales 1-7 del ADR 007, que hace el usuario
Paralelizable: no
Origen: PLAN.md criterio de cierre de F6 "Juego en el dominio vía Cloudflare Tunnel, con WebSocket"; ADR 007.
Riesgo: alto (primera exposición a Internet)

Objetivo: el juego funciona en el hostname público (U1) desde el clon de prod (U2), con el WebSocket, y vuelve solo después de un reinicio.

Pasos: el usuario completa `.env` en el clon de prod (el central nunca ve ni escribe los valores) y corre `docker compose up -d --build`. El central verifica:

Criterios de aceptación:
- `docker compose ps` → `db`, `app`, `tunnel`, `backup` en `running` (`app` y `db` `healthy`); `docker compose logs tunnel` muestra conexiones registradas.
- `curl -s -o /dev/null -w '%{http_code}' https://<host>/` → `200`; `curl -sD- -o /dev/null https://<host>/ | grep -iE 'content-security-policy|strict-transport-security'` → las dos cabeceras, la CSP con `connect-src 'self'`.
- `https://<host>/swagger` y `/docs` → 404. `http://<host>/` redirige a `https`.
- Desde el exterior no hay puertos: `docker compose config | grep ports` sin salida; `netstat -ano | grep -E ':(5432|8080) '` sin LISTEN en el host.
- En dos navegadores (o uno normal y otro privado), por el dominio: registrar dos jugadores, crear y unirse a una partida, colocar barcos, jugar un turno, emote. En las herramientas de desarrollo: WebSocket `wss://<host>/the-last-shipbender` con estado `101`, frames de heartbeat cada ~10 s, sin violaciones de CSP. Dejar la partida abierta 3 minutos sin tocar: el socket sigue vivo (Cloudflare corta conexiones ociosas a los ~100 s; los heartbeats lo evitan).
- `select count(*) from player` = 2 (los dos de la prueba; ningún usuario por defecto).
- Aparece un dump en `BACKUP_DIR` (U4).
- El usuario reinicia Windows e inicia sesión: en ≤ 3 min el dominio responde 200 sin intervención.
- Resultado en esta tarea y propuesta al usuario de cerrar F6 (merge a `main` solo con su aprobación).

## Resultado
