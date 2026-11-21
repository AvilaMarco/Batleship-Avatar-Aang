# 512 · reviewer · Revisión de riesgo alto de F5 (JWT en el cliente y rutas públicas)
Estado: pendiente
Fase: F5
Depende de: 503, 504, 509, 510 (antes del merge de F5; puede ir en paralelo con 511)
Paralelizable: sí, con 511 (solo lee)
Origen: CLAUDE.md "Riesgo alto (seguridad...): tarea de `reviewer` antes del merge"; tareas 503, 504, 509, 510.
Riesgo: alto

Objetivo: revisar (solo lectura) los diffs de esas tareas y reportar hallazgos; no editar código.

Archivos a revisar: `git log --oneline main..HEAD` y `git show` de los commits de 503, 504, 509, 510; `frontend/src/api/**`, `frontend/src/auth/**`, `frontend/src/screens/LoginScreen.tsx`, `WebSecurityConfig.java`, `pom.xml`.

Lista de chequeo:
- Token solo vía `api/auth.ts` (`sessionStorage` en dev, `localStorage` en prod); nunca en URL, query, logs (`console.*`), estado serializado ni mensajes de error.
- 401 borra el token; logout borra el token; nada asume que el token es válido sin el `GET /api/players/me`.
- STOMP manda `Authorization` en CONNECT; no hay suscripciones fuera de `/topic/games/{id}` y `/user/queue/errors`.
- Sin `dangerouslySetInnerHTML`, `innerHTML` ni `eval`; texto del servidor (`detail`, nombres, emotes) renderizado como texto; el emote solo se usa si está en la lista conocida (no se arma una URL con texto del rival sin validar).
- `WebSecurityConfig`: lista pública cerrada (sin `/**`, sin `permitAll` nuevo en `/api`); los tests de 509 cubren lo agregado y lo quitado.
- `pom.xml`: el perfil `frontend` no se activa por defecto; no hay secretos ni `.env` en el build.

Criterios de aceptación:
- `## Resultado` con hallazgos clasificados (bloqueante / menor / ok) con archivo:línea. Cada bloqueante → tarea nueva `5NN-<rol>-<tema>.md` (pendiente).
- Sin commits de código (solo la sección Resultado y las tareas nuevas, en un commit `docs: revisión de seguridad F5 (tarea 512)`).

## Resultado
