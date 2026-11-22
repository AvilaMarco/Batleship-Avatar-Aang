# 612 · reviewer · Revisión de riesgo alto: exposición en loopback, SSH restringido y despliegue
Estado: hecha
Fase: F7
Depende de: 610, 611
Paralelizable: no
Origen: CLAUDE.md "riesgo alto → reviewer"; tareas 610 y 611.
Riesgo: alto

Objetivo: revisión SOLO LECTURA antes de usar el despliegue en el servidor real.

Lista mínima: (a) `ports:` solo en loopback para `app`, nada más publicado; (b) `ssh-entry` (lista blanca, inyección por `SSH_ORIGINAL_COMMAND`, TAG validado, comillas, permisos de ficheros, rutas); (c) `deploy.sh` no ejecuta nada derivado de datos externos; (d) el rollback automático no puede dejar `app` sin versión; (e) `.ship.env` ignorado por git y sin secretos en los scripts; (f) el flujo Caddy → app: `X-Forwarded-Proto`/`Host`, qué pasa si Caddy no fija la cabecera (403 en el WebSocket) y si un cliente la falsea; (g) Cloudflare Access: el juego es público y necesita Bypass; (h) backups: ¿dónde acaban?, permisos 0600, y el riesgo de que estén en el mismo disco; (i) RAM del servidor (~2,6 GiB disponibles con swap en uso): límites de memoria del compose (app 768m, db 512m).

Criterio: informe con BLOQUEANTE/MENOR/OK y veredicto; el central lo transcribe en el Resultado y crea tareas si hay bloqueantes.

## Resultado

Revisor: rol `reviewer` (solo lectura; el central transcribió el informe).

**Veredicto: cambios requeridos (1 bloqueante pequeño).** Con ese arreglo se puede usar contra el servidor real siguiendo el runbook.

BLOQUEANTE
1. `scripts/deploy.sh:8` `trap '' HUP` no basta si se corta el SSH: al caer la sesión se cierran las tuberías de stdout/stderr y el siguiente `echo` mata el script con SIGPIPE (reproducido en Git Bash). Si la conexión cae durante `wait_healthy` (hasta 180 s) y la versión nueva no llega a healthy, el `echo "ERROR..."` (línea 110) mata el script ANTES del rollback (112-117): queda `.current_tag` con la versión mala y `app` en crashloop. → tarea 613.

MENOR
2. La clave restringida equivale a root del servidor (el grupo `docker` = root; `infra` sustituye el compose, `load` acepta cualquier imagen del tar). Es riesgo aceptado: documentarlo ("clave = root del servidor") y recomendar `from="IP"`, clave solo para este fin y usuario sin sudo ni shell interactivo. → 613 (docs).
3. `infra` (80-81): un nombre de fichero con salto de línea puede colar líneas que parecen válidas; sin impacto real (el autor del tar ya puede escribir scripts) pero arreglo barato: rechazar si `tar -tf | wc -l` difiere del número de entradas de `tar -tvf`. → 613.
4. Sin límite de tamaño en `infra`/`load` (llenar el disco). Mismo nivel de confianza que el punto 2; añadir un tope barato. → 613.
5. `load` e `infra` no toman el `lock`: pueden interferir con un `up` concurrente. → 613.
6. `scripts/backup.sh:55` está en ISO-8859 (comentario corrupto); pasarlo a UTF-8. → 613.
7. Cada `up` genera un dump; con muchos despliegues al día se acumulan (retención por días). Nota en el runbook, y que el usuario de despliegue no puede leerlos (0600 root). → 613 (docs).
8. `mem_limit` sin `memswap_limit`: con swap en el host (el servidor ya usa 1,1 GiB) un contenedor puede usar swap hasta el doble de su límite. Declarar `memswap_limit` igual a `mem_limit`. → 613.

OK (verificado por el revisor y por el central): `ssh-entry` con lista blanca estricta (11 entradas hostiles del revisor + 9 del central, todas rechazadas con 126 y sin crear el archivo canario), sin `eval`/`sh -c`/interpolación; `restrict,command=` deja un único comando; `infra` valida antes de extraer (solo ficheros planos y directorios, sin symlinks/hardlinks/rutas absolutas ni `..`); `up` falla antes de tocar nada si falta una imagen; el backup previo aborta el despliegue si falla; rollback limpio sin `.previous_tag` o con imagen borrada; `flock` en `up` y `rollback`; compose: `app` solo en 127.0.0.1, `db`/`backup` sin puertos, `tunnel` bajo profile; secretos ignorados por git; `ship.sh` sin `StrictHostKeyChecking=no`.

No verificado: rollback real, `flock` y `check_images` (revisión estática), comportamiento de `sshd` con `restrict,command=`, el handshake detrás del Caddy real, nombre con salto de línea en tar (Windows no lo permite), tamaño real de RAM y disco del servidor.
