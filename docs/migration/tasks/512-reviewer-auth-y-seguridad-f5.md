# 512 · reviewer · Revisión de riesgo alto de F5 (JWT en el cliente y rutas públicas)
Estado: hecha
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

Revisor: rol `reviewer` (solo lectura; el central transcribió el informe y verificó dos afirmaciones: `favicon.svg` enlazado en `index.html` y sin tope de longitud en `password`).

**Veredicto: aprobado con menores. Sin bloqueantes.** `npm audit`: 0 vulnerabilidades. `npm test`: 67/67.

MENOR
1. `frontend/src/api/stomp.ts:68,71`: el token se lee una sola vez; con `reconnectDelay: 5000` y un token caducado el CONNECT se reintenta sin fin y sin avisar. → tarea 513.
2. Falta `Content-Security-Policy` (Spring Security no la añade por defecto). Es la mitigación principal para guardar el token en `localStorage` en producción (decisión U2). → tarea 514 (o Cloudflare en F6).
3. `frontend/src/api/auth.ts:3`: `localStorage` en prod es legible por cualquier XSS. Riesgo hoy bajo (sin `innerHTML`/`eval`, 2 dependencias de runtime, texto como texto, emotes de lista cerrada), y `sessionStorage` tampoco evita un XSS. Mitigación: CSP + considerar bajar `JWT_EXPIRATION_MINUTES` en prod (24 h sin revocación).
4. `WebSecurityConfig.java:38`: `/favicon.svg` no está permitido (el `index.html` lo enlaza). Cosmético. → tarea 514.
5. `LoginScreen.tsx`: sin `maxLength`; el servidor limita el email (`@Size(max=30)`) pero `password` y `name` solo tienen `@NotEmpty` (`RegisterRequest.java:7`, `LoginRequest.java:7`). → tarea 514 (`@Size`, p. ej. password ≤ 72 por el límite de bcrypt).
6. `GameScreen.tsx:44`: `setError(p.detail)` muestra texto del servidor; se renderiza como texto, aceptable.

OK (verificado): token solo vía `api/auth.ts` y nunca en URL/logs/errores; 401 borra token y redirige sin bucle; logout borra; STOMP manda `Authorization` y solo se suscribe a `/topic/games/{id}` y `/user/queue/errors` (reforzado en el servidor); CONNECT sin usuario se rechaza; origen del WebSocket por defecto (mismo origen); lista pública cerrada, sin `/**`; emote solo de lista conocida; perfil `frontend` no activo por defecto.

No verificado: comportamiento en vivo de las rutas públicas (lo cubren los tests de 509/510), contenido de `/docs/**`, validación de longitud en servidor más allá de lo leído, cloudflared (F6).
