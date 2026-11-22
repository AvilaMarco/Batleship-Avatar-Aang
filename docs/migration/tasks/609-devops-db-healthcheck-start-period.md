# 609 · devops · Healthcheck de la base de datos con `start_period`
Estado: pendiente
Fase: F7
Depende de: —
Paralelizable: sí
Origen: medición de recursos del deploy (2026-11-21): en 1 de 4 arranques en frío el `docker compose up` falló con "dependency failed to start: container db is unhealthy". El registro de salud mostró que `pg_isready` responde "no response" ~20 s durante la inicialización del clúster de Postgres, y el healthcheck de `db` no tiene `start_period` (retries 5 x interval 10 s = 50 s de margen).
Riesgo: normal

Objetivo: que un arranque en frío lento (disco o CPU cargados) no haga fallar `docker compose up`.

Archivos: `docker-compose.yml` (solo el healthcheck de `db`), `docs/deploy/runbook.md` (una línea en "Problemas conocidos").

Diseño (cerrado): añadir `start_period: 60s` al healthcheck de `db` (los fallos durante el start_period no cuentan) y subir `retries` a 6. No tocar otros servicios.

Criterios de aceptación (proyecto aislado `COMPOSE_PROJECT_NAME=shipbender-test`, `MSYS_NO_PATHCONV=1`, `.env` de prueba no versionado, `BACKUP_DIR` temporal fuera del repo):
- `docker compose config -q` sin errores.
- Arranque en frío x3 (`down -v` entre medias, con echo previo del nombre del proyecto): `db` y `app` llegan a `healthy` y `docker compose up -d db app` termina con exit 0 las 3 veces.
- `docker inspect` muestra `StartPeriod` de 60 s.
- Limpieza: `down -v` del proyecto de prueba, borrar `.env` de prueba y carpeta temporal.
- Commit `build: start_period en el healthcheck de db (tarea 609)`.

## Resultado
