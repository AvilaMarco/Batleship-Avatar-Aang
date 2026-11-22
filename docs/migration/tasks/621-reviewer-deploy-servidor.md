# 621 · reviewer · Revisión del despliegue adaptado al servidor
Estado: hecha (cambios requeridos)
Fase: F7
Depende de: 620
Paralelizable: no
Origen: CLAUDE.md "riesgo alto → reviewer antes del merge"; tarea 620.
Riesgo: alto

Revisa (solo lectura): `docker-compose.yml`, `deploy/servidor.env`, `scripts/ship.sh` y los restos de `scripts/`, contra los invariantes del servidor (sin privileged/docker.sock/host/cap_add, `cap_drop: ALL`, no-new-privileges, no root, límites, volúmenes bajo `data/`, tags fijos, sin secretos en repo/imagen). Busca: secretos filtrados en el paquete o la imagen (`.dockerignore`, historial), el paquete que el runner root consume (rutas relativas, enlaces, tamaño), rollback con migración Flyway, y que el WebSocket siga funcionando tras Caddy. Entrega hallazgos BLOQUEANTE/MENOR en `## Resultado`.

## Resultado
Veredicto: cambios requeridos. Solo lectura; no se ejecutó `ship.sh --dry-run` ni Docker.

BLOQUEANTE
1. `scripts/ship.sh:40-44`, `compose.yaml:6`: el paquete solo lleva la imagen de la app; `db` usa `postgres:17.11-alpine@sha256:…` y el servidor no hace pull. `ship.sh` ya no tiene `--with-db` (`deploy.sh:45` y `runbook.md:95` aún lo citan). Una imagen cargada con `docker load` suele perder el digest, y entonces `image:tag@sha256` no resuelve. Primer `up` falla. Acción: preguntar al admin si tiene postgres precargado y cómo lo identifica; si no, añadir `postgres.tar.gz` al paquete (ampliar `recibir`) o quitar el `@sha256`. No verificable hasta que responda.

MENOR
2. `scripts/deploy.sh` (líneas 3, 45, 51, 75, 189): obsoleto (`ssh-entry`, servicio `backup`, `--with-db`) y se envía al admin en `RESPUESTA-AL-SERVIDOR-2.md:13`. Marcarlo no ejecutable o moverlo a `docs/`.
3. `docs/deploy/runbook.md:98-121`, `docs/deploy/CHECKLIST-SERVIDOR.md:5`: describen el flujo viejo. Reescribir antes de cerrar F7.
4. Rollback con Flyway: `V3__limpieza_f4.sql` limpia datos/columnas y la app usa `ddl-auto=validate`; volver a una imagen anterior sobre un esquema migrado puede no arrancar. Confirmar y documentar que el backup previo se restaura a mano (migraciones expand/contract).
5. `compose.yaml:13`: `db` corre como `70:70` con `read_only`; `data/pgdata` debe existir con dueño 70 y modo 0700. Añadirlo a `PRIMER-DESPLIEGUE.md` y al checklist.
6. `compose.yaml:32` (`user: 10001:10001`) vs `Dockerfile:98` (uid de sistema): alinear con `adduser -u 10001`.
7. `scripts/ship.sh:32,40`: con `--force` el tag es el sha de HEAD aunque haya cambios sin commitear. Aviso existente; riesgo aceptable.

Verificado OK: `compose.yaml` cumple los invariantes (sin privileged/docker.sock/host/cap_add, `cap_drop ALL`, no-new-privileges, no root, `read_only`+tmpfs, límites, volumen bajo `./data/`, sin `latest`, secretos solo como `${…}`); sin secretos en imagen ni historial; paquete de 3 archivos planos con tope 1 GiB; `forward-headers-strategy=native` y mismo origen para el WebSocket (falta la prueba real del 101, paso 6 de `PRIMER-DESPLIEGUE.md`).
