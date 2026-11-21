# 604 · devops · Backups de Postgres: backup.sh, restore.sh y prueba de restauración
Estado: hecha
Fase: F6
Depende de: 603
Paralelizable: no (usa el stack de prueba de 603)
Origen: ADR 007 punto 10; pedido del usuario "backups de Postgres (pg_dump programado, retención, dónde, y cómo se restaura: un script `scripts/backup.sh` y `scripts/restore.sh` mínimos)".
Riesgo: alto (datos) → revisión en 606

Objetivo: el servicio `backup` hace un dump al arrancar y luego cada 24 h, con retención de 14 días, y `restore.sh` deja la BD igual al dump.

Archivos: `scripts/backup.sh`, `scripts/restore.sh` (contenido del ADR 007 Anexo D, en LF).

Verificación: mismo proyecto aislado que en 603 (`COMPOSE_PROJECT_NAME=shipbender-test`, `MSYS_NO_PATHCONV=1`, `.env` de prueba con `BACKUP_DIR` en una carpeta temporal fuera del repo).

Criterios de aceptación:
- `git ls-files --eol scripts/` → los dos en `w/lf`.
- `docker compose up -d db app backup` → en `BACKUP_DIR` aparece `shipbender-*.dump` y `docker compose logs backup` muestra `backup ok`.
- Retención: crear `BACKUP_DIR/shipbender-20000101-000000.dump` y darle fecha vieja (`docker compose exec backup touch -d '2000-01-01' /backups/shipbender-20000101-000000.dump`). Después de `docker compose exec backup sh /scripts/backup.sh` ese archivo ya no está.
- Restauración: registrar un jugador (`curl` desde la red `edge` a `POST /api/players`) → backup → registrar un segundo jugador → `scripts/restore.sh <dump del primero>` (responder `si`) → `select count(*) from player` = 1, y `app` vuelve a `healthy`.
- Un nombre de archivo inexistente → `restore.sh` sale con error antes de detener `app`.
- Limpieza como en 603 (`down -v` solo del proyecto de prueba; borrar la carpeta temporal y el `.env` de prueba).
- Commit `build: backups programados de Postgres y script de restauración (tarea 604)`.

## Resultado
Scripts en `scripts/backup.sh` y `scripts/restore.sh` (LF, `git ls-files --eol` → `w/lf`). Verificado en el proyecto aislado `shipbender-test` con `BACKUP_DIR` temporal fuera del repo (ya borrado, igual que el `.env` de prueba, el stack con `down -v` y la imagen):
- Dump al arrancar: `shipbender-*.dump` creado y `backup ok: /backups/...` en `docker compose logs backup`.
- Retención: `shipbender-20000101-000000.dump` con fecha 2000-01-01 desapareció tras `exec backup sh /scripts/backup.sh`.
- Restauración: 1 jugador → dump → 2º jugador (count=2) → `restore.sh` + `si` → count=1 y `app` vuelve a `healthy`.
- Archivo inexistente, nombre con `/`, dump ilegible o truncado → error antes de parar `app` (sigue `healthy`). Responder distinto de `si` → cancela, `app` sigue arriba.
- Fallo a mitad (dump válido que no puede aplicarse: `DROP TABLE player` falla por FKs): se muestra el error, la transacción se revierte, count sigue en 2, `app` se arranca de nuevo y el mensaje indica el dump de seguridad.

Correcciones al diseño del Anexo D (fallos reales detectados):
1. Con `set -e`, si fallaba el dump de seguridad o `pg_restore`, `app` quedaba detenida sin aviso y con la BD posiblemente a medias. Ahora `pg_restore` va con `--single-transaction --exit-on-error` (todo o nada: la BD queda restaurada o intacta) y un `trap EXIT` arranca `app` siempre; el script dice claramente qué pasó y el nombre del dump de seguridad.
2. El dump de seguridad ejecutaba la retención y podía borrar el propio archivo a restaurar si tenía más de 14 días (comprobado con fecha vieja). Ahora se llama con `RETENTION_DAYS=36500`.
3. `docker compose exec -T` consumía el stdin y el `read` de confirmación recibía EOF (salía en silencio). `exec` ahora con `</dev/null`.
4. Validación previa con `pg_restore -l` (dump corrupto se rechaza antes de parar `app`) y rechazo de nombres con `/` o `\`.
5. `backup.sh` borra su `.tmp` si `pg_dump` falla (trap).
Limitaciones: Ctrl-C en mitad de `restore.sh` puede dejar el `pg_restore` corriendo dentro del contenedor mientras `app` arranca (docker exec sin tty no propaga la señal); la transacción sigue siendo atómica pero no se probó. `BACKUP_DIR` en `.env` debe ser ruta absoluta del servidor (o `./backups`); los scripts no dependen de ninguna ruta.
