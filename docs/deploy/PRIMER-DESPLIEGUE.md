# Primer despliegue real (tarea 607) — tabla y guion

Estado: **borrador**. Depende de la respuesta del administrador a `RESPUESTA-AL-SERVIDOR.md` (preguntas 1-10) y de la tarea 620 (+ revisión 621). Los comandos exactos del paso 4 se fijan al conocer `recibir`.

## Tabla de la respuesta del servidor
| # | Ítem | Estado | Qué cambia en el proyecto | Quién |
|---|---|---|---|---|
| A1-4 | Usuario `deploy` (git-shell, sin docker), SSH desde .221, `restrict` | adaptar | `ship.sh` usa `tva-deploy` y `recibir`; adiós `deploy.sh ssh-entry` (ahora en `docs/deploy/legacy/`) y clave en grupo docker | 620 |
| B5-6 | Carpetas `/opt/compose/apps/*`; secretos generados en el servidor | adaptar | `.env` sale del repo/guion; solo nombres de variables | admin + 620 |
| C7-8 | Puerto 3100; alta con `server-app-new/-public/-kuma`; manifiesto | falta | `deploy/servidor.env` | 620 |
| D9-12 | Caddy ya fija `X-Forwarded-Proto https`; WS reloads | OK/adaptar | `native` se mantiene; verificar 101 | 607 |
| E13-15 | Ruta + Access Bypass; SSL strict; rate limit | falta | rate limit 10/10 s; limitador en la app (mejora aparte, sesión principal) | admin |
| F16 | Sin salida para la app | adaptar | nada (no usa terceros) | admin |
| G17-20 | Docker 29, cgroup v2 (memswap OK), subred 10.240.160.0/24, RAM justa | adaptar | `app` 768m, `db` 256m, JVM/PG/Hikari; sin `edge` | 620 |
| H21-22 | `server-backup` + dump previo; externa más adelante | adaptar | quitar servicio `backup` y scripts | 620 |
| I23-24 | Arranque OK; Kuma; salud | OK/falta | `SALUD=/`; app tolera BD tardía (Hikari reintenta) | admin |
| J25-26 | Sin ufw; sin reinicio automático | adaptar | `read_only`+tmpfs, `cap_drop`, no root | 620 |
| K27 | Flyway expand/contract; tags fijos; no desplegar 03:00/04:30 | — | regla de equipo | — |

## Pre-requisitos en el servidor (tarea 622)
- **Postgres precargada**: `postgres:17.11-alpine` (tag exacto, sin digest) debe estar en el Docker del servidor antes del primer `up`; el paquete no la lleva y el servidor no hace pull. Precarga única por `docker load` o `docker pull` del admin; comprobar con `docker image inspect postgres:17.11-alpine`.
- **`data/pgdata` con dueño 70:70 y modo 0700** (uid del usuario `postgres` en Alpine): `install -d -o 70 -g 70 -m 0700 data/pgdata` antes del primer `up`. Si no, Postgres no arranca por permisos.
- **Usuario de la app 10001:10001**: la imagen crea el usuario `app` con uid/gid 10001 (coincide con `user:` del compose).

## Guion (qué hace cada quien y qué se verifica)
| Paso | Quién | Acción | Verificación (yo) |
|---|---|---|---|
| 0 | usuario | Pasa `RESPUESTA-AL-SERVIDOR.md` y me trae la contestación | adapto 620 |
| 1 | yo | Implemento 620, pruebo en Docker local, 621 revisa; commits | `docker compose config -q`, 101/403, `docker stats`, bloqueantes = 0 |
| 2 | admin | `server-app-new shipbender docker`; secretos en `secrets.env`; redes/firewall para 10.240.160.0/24; clave `from=192.168.0.221` | usuario me trae la salida (sin secretos) |
| 3 | admin | Aprueba el compose (`server-app-aprobar`); `server-app-public shipbender`; `server-app-kuma shipbender`; ruta de Cloudflare + Access Bypass + rate limit 10/10 s | — |
| 4 | usuario | En `main`, árbol limpio, `./mvnw test`; `scripts/ship.sh` (compila y envía el paquete) | salida de `ship.sh` y `server-logs app-shipbender`: `healthy` o rollback |
| 5 | usuario | Desde cualquier red: `curl -sI https://shipbender.racnar.dev/` | 200 de la app, **no** 302 a `cloudflareaccess.com` |
| 6 | usuario | Handshake WS: mismo `curl` de `runbook.md` §1 contra el host público con `-H 'Origin: https://shipbender.racnar.dev'` | 101 (si 403: Proto/Origin); con origen ajeno 403 |
| 7 | usuario | Registrar 2 jugadores en dos navegadores, jugar un turno, emote; 3 min inactivo | WS vivo, sin violaciones de CSP; `/swagger` 404 |
| 8 | admin | `docker exec app-shipbender-db psql -c 'select count(*) from player'`; `server-backup` manual y comprobar el dump | 2 jugadores; dump y restauración de prueba ok |
| 9 | admin | Reinicio del servidor (fuera de 03:00/04:30) | en ≤ 3 min el dominio da 200 sin intervención; db y app `healthy` |
| 10 | yo | Escribo `## Resultado` en 607, actualizo PLAN y propongo cerrar F7 (merge solo con tu aprobación) | — |

Regla: yo nunca entro al servidor ni veo secretos; el usuario o el admin ejecutan y me traen la salida.
