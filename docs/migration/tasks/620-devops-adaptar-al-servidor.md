# 620 · devops · Adaptar el despliegue al diseño del servidor (paquete + runner root)
Estado: pendiente (bloqueada por la respuesta del administrador a `docs/deploy/RESPUESTA-AL-SERVIDOR.md`, preguntas 1-6)
Fase: F7
Depende de: 611, 613; respuesta del administrador
Paralelizable: no
Origen: respuesta del servidor (2026-11-22): "ninguna clave remota tiene Docker ni sudo"; decisión del usuario: "Sí, adoptarlo" (diseño del servidor), backup externo (b) más adelante, `from=192.168.0.221`.
Riesgo: alto (contenido que ejecuta root en el servidor y secretos) → tarea 621 (reviewer) antes de usar.

Objetivo: que el repo entregue lo que el servidor consume: un paquete (imagen + `compose.yaml` + `deploy/servidor.env`) que `recibir` valida y un runner root despliega, con un compose que pase los invariantes del servidor.

Archivos: `docker-compose.yml`, `deploy/servidor.env` (nuevo), `scripts/ship.sh`, `scripts/deploy.sh` (reutilizable como base del runner o retirada), `scripts/backup.sh` y `scripts/restore.sh` (retirar si el servidor cubre el backup), `.env.example`, `.ship.env.example`, `docs/adr/007-deploy.md`, `docs/deploy/runbook.md`.

Diseño (confirmar con la respuesta del servidor):
1. Compose: sin `ports:`, `edge`, `tunnel` ni `backup`; `name: app-shipbender`, servicios `app` y `db`, tags fijos (`postgres:17.x` exacto; app `shipbender:${APP_TAG}` = sha); `cap_drop: [ALL]`, `security_opt: [no-new-privileges:true]`, `pids_limit`, `read_only: true` + `tmpfs: /tmp`, `mem_limit`/`memswap_limit` (app 768m, db 256m), `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k`, `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=8`, Postgres con `-c shared_buffers=64MB -c max_connections=20`, label de backup que indique el servidor, volumen bajo `data/` según su convención.
2. `deploy/servidor.env`: campos no secretos que acuerde el servidor (puerto interno 8080, `SALUD=/`, memoria, Postgres, nombres de secretos).
3. `ship.sh`: compilar `linux/amd64`, armar el paquete y enviarlo con el alias `tva-deploy` (`tar … | ssh … recibir shipbender <tag>`); sin `docker load`/`infra`/`ssh-entry` remotos.
4. Retirar de `.env.example` lo que genera el servidor (secretos, `APP_PORT`, `BACKUP_DIR`, `TUNNEL_TOKEN`) y de `.ship.env.example` lo que sobre.

Criterios de aceptación (proyecto aislado `COMPOSE_PROJECT_NAME`, puertos 4756, `.env` de prueba no versionado):
- `docker compose config -q` pasa con un `.env` de prueba; no queda `ports:`, `latest`, `privileged`, `docker.sock` ni `network_mode: host`.
- La app arranca con `read_only` y `cap_drop: ALL` (Tomcat/Spring sin error de escritura) y `healthy`; Postgres también.
- Con una red `internal` y proxy de prueba en 127.0.0.1 con `X-Forwarded-Proto: https`: handshake WebSocket 101 y 403 con origen ajeno (confirma que `native` confía en la IP del gateway de la red del servidor).
- Memoria en reposo medida con `docker stats` ≤ límites (app ≤ 768m, db ≤ 256m).
- `ship.sh` genera un paquete con la estructura acordada (probar con un `recibir` simulado local que valide nombre/tag/tamaño).
- ADR 007 y runbook actualizados con los hechos reales; secciones obsoletas (túnel propio, `deploy.sh ssh-entry`, `backup`) retiradas.
- Commit `refactor(deploy): paquete + compose endurecido para el runner del servidor (tarea 620)`.

## Resultado
