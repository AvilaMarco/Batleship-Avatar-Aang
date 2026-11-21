# 604 · devops · Backups de Postgres: backup.sh, restore.sh y prueba de restauración
Estado: pendiente
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
