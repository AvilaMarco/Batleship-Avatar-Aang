# 620 · devops · Adaptar el despliegue al diseño del servidor (paquete + runner root)
Estado: parcial (compose, manifiesto y ship.sh probados en local; falta recibir/validador reales del servidor y la revisión 621)
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
Hecho (2026-11-22), según la Respuesta 2 del servidor (contrato `paquete`, v1):
- `compose.yaml` (renombrado; sin name/ports/build/edge/backup/tunnel/`latest`): `db` con `postgres:17.11-alpine@sha256:e5d2edd2…` (17 días), `user 70:70`, read_only + tmpfs, label `tva.backup: postgres`; `app` con `shipbender/app:${VERSION}`, `user 10001`, read_only + tmpfs `/tmp`, Hikari 8, `cap_drop ALL`, no-new-privileges, `mem_limit = memswap_limit` (768m/256m), `pids_limit`. Redes `web`/`interna` sin definir (las pone el override del servidor).
- `deploy/servidor.env` (manifiesto de 9 campos) y `deploy/compose.local.yaml` (imita el override del servidor SOLO para pruebas; usa volumen con nombre porque NTFS no admite chmod en bind mounts).
- `scripts/ship.sh`: compila `shipbender/app:<sha>`, arma el tar sin comprimir (servidor.env, compose.yaml, imagenes.tar.gz), valida espejo de las reglas de `recibir` (tres nombres, sin carpetas, ≤ 1 GiB, APP, nombre de imagen) y envía con `ssh tva-deploy recibir shipbender <sha>`; mapea códigos 0-4; `--dry-run`. `Dockerfile`: JVM por `JAVA_TOOL_OPTIONS` (70 %, SerialGC, Xss512k) en vez de `-XX:MaxRAMPercentage=75` en el ENTRYPOINT. Retirados `backup.sh`/`restore.sh`; `deploy.sh` queda solo como referencia del runner. `.env.example`/`.ship.env.example` reducidos a pruebas locales / alias.
Verificado en local (proyecto `app-shipbender-test`, 127.0.0.1:4756): `config -q` ok; `db` y `app` healthy con read_only y cap_drop ALL; `/` 200; WebSocket 101 con Origin propio + `X-Forwarded-Proto: https`, 403 con origen ajeno y 403 sin la cabecera (`native` confía en el gateway de la red Docker); `/swagger` 404; memoria en reposo app 253 MiB / 768, db 45 MiB / 256, pids 36/14; paquete de 137 MB (131 MB de imagen) validado con `ship.sh --dry-run`; limpieza hecha.
Falta / no verificado: el `recibir` y el validador reales (el compose puede ser rechazado por una regla que no conozco), cómo el runner inyecta `${VERSION}` (SUPUESTO, preguntado al administrador), bind mount `./data/pgdata` con uid 70 (solo en Linux), ssh real, `from=` y `tva-deploy`.
