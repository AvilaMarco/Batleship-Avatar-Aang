# 606 · reviewer · Revisión de riesgo alto de F6 (secretos, red, proxy, backups)
Estado: pendiente
Fase: F6
Depende de: 601, 602, 603, 604 (antes de exponer el dominio en 607)
Paralelizable: sí, con 605 (solo lee)
Origen: CLAUDE.md "Riesgo alto (seguridad...): tarea de `reviewer`"; tareas 602, 603, 604.
Riesgo: alto

Objetivo: revisar (solo lectura) y reportar hallazgos BLOQUEANTE / MENOR antes de poner el juego en Internet. No editar.

Archivos a revisar: `git log --oneline main..HEAD` y `git show` de 601-604; `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example`, `.gitignore`, `scripts/*.sh`, `application*.properties`, `WebSecurityConfig.java`, ADR 007.

Lista mínima:
- Ningún secreto versionado: `git log -p main..HEAD | grep -iE 'JWT_SECRET=.|PASSWORD=.|TUNNEL_TOKEN=.'` solo muestra claves vacías o de ejemplo; `.env` ignorado (`git check-ignore .env`); `.dockerignore` excluye `.env`, así que no entra en la imagen.
- Sin `ports:` en el compose; `db` y `backup` solo en la red `internal`; versiones de imagen fijadas (sin `latest`, salvo `shipbender:${APP_TAG}`).
- La imagen corre como no root, con `SPRING_PROFILES_ACTIVE=prod`, sin devtools y sin seeder.
- `forward-headers-strategy=native`: confirmar que no se puede falsificar `X-Forwarded-*` desde fuera (solo entra cloudflared) y que la regla de mismo origen sigue activa (test 2 de 602).
- CSP: `connect-src 'self'` sin `ws:`/`wss:` comodín.
- Backups: `restore.sh` pide confirmación, hace un dump de seguridad antes y valida el archivo antes de detener `app`; los dumps no quedan dentro del repo ni de la imagen.
- `JWT_EXPIRATION_MINUTES` por defecto 480; Swagger apagado en prod.

Criterio de aceptación: sección `## Resultado` con los hallazgos (archivo:línea, severidad, propuesta) y un veredicto. Si hay BLOQUEANTES, el central crea las tareas de arreglo antes de 607.

## Resultado
