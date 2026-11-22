# 621 · reviewer · Revisión del despliegue adaptado al servidor
Estado: pendiente
Fase: F7
Depende de: 620
Paralelizable: no
Origen: CLAUDE.md "riesgo alto → reviewer antes del merge"; tarea 620.
Riesgo: alto

Revisa (solo lectura): `docker-compose.yml`, `deploy/servidor.env`, `scripts/ship.sh` y los restos de `scripts/`, contra los invariantes del servidor (sin privileged/docker.sock/host/cap_add, `cap_drop: ALL`, no-new-privileges, no root, límites, volúmenes bajo `data/`, tags fijos, sin secretos en repo/imagen). Busca: secretos filtrados en el paquete o la imagen (`.dockerignore`, historial), el paquete que el runner root consume (rutas relativas, enlaces, tamaño), rollback con migración Flyway, y que el WebSocket siga funcionando tras Caddy. Entrega hallazgos BLOQUEANTE/MENOR en `## Resultado`.

## Resultado
