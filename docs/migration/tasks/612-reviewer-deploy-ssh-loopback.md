# 612 · reviewer · Revisión de riesgo alto: exposición en loopback, SSH restringido y despliegue
Estado: pendiente
Fase: F7
Depende de: 610, 611
Paralelizable: no
Origen: CLAUDE.md "riesgo alto → reviewer"; tareas 610 y 611.
Riesgo: alto

Objetivo: revisión SOLO LECTURA antes de usar el despliegue en el servidor real.

Lista mínima: (a) `ports:` solo en loopback para `app`, nada más publicado; (b) `ssh-entry` (lista blanca, inyección por `SSH_ORIGINAL_COMMAND`, TAG validado, comillas, permisos de ficheros, rutas); (c) `deploy.sh` no ejecuta nada derivado de datos externos; (d) el rollback automático no puede dejar `app` sin versión; (e) `.ship.env` ignorado por git y sin secretos en los scripts; (f) el flujo Caddy → app: `X-Forwarded-Proto`/`Host`, qué pasa si Caddy no fija la cabecera (403 en el WebSocket) y si un cliente la falsea; (g) Cloudflare Access: el juego es público y necesita Bypass; (h) backups: ¿dónde acaban?, permisos 0600, y el riesgo de que estén en el mismo disco; (i) RAM del servidor (~2,6 GiB disponibles con swap en uso): límites de memoria del compose (app 768m, db 512m).

Criterio: informe con BLOQUEANTE/MENOR/OK y veredicto; el central lo transcribe en el Resultado y crea tareas si hay bloqueantes.

## Resultado
