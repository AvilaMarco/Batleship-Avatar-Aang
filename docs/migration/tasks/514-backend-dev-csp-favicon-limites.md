# 514 · backend-dev · CSP, favicon.svg y límites de longitud
Estado: hecha
Fase: F5
Depende de: 512
Paralelizable: sí con 513 (solo backend); no con otra tarea que ejecute Maven
Origen: hallazgos MENOR 2, 4 y 5 de la tarea 512.
Riesgo: alto (cabeceras y reglas de seguridad)

Objetivo: añadir la cabecera CSP que protege el token en `localStorage`, permitir el favicon real y acotar la entrada.

Archivos: `security/WebSecurityConfig.java`, `player/dto/RegisterRequest.java`, `player/dto/LoginRequest.java`, tests en `src/test/.../integration/SecurityRulesIntegrationTest.java` (excepción explícita: solo AÑADIR casos, no editar los existentes).

Diseño (cerrado):
- `headers.contentSecurityPolicy("default-src 'self'; connect-src 'self' ws: wss:; img-src 'self' data:; style-src 'self' 'unsafe-inline'; frame-ancestors 'none'; base-uri 'self'")`. Comprobar a mano que el front y Swagger UI de dev siguen funcionando (Swagger necesita scripts/estilos propios: si la CSP lo rompe, excluir esa ruta con una política más laxa solo para `/swagger-ui/**` y `/docs/**`, y decirlo en el Resultado).
- `/favicon.svg` se suma a la lista pública estática.
- `@Size(max = 72)` en `password` y `@Size(max = 50)` en `name` (límite de bcrypt para la contraseña); mensajes por `VALIDATION_FAILED`.

Criterios de aceptación:
- Tests nuevos (primero rojo): `/favicon.svg` sin token no es 401/403; la respuesta de `/` lleva `Content-Security-Policy`; registro con contraseña de 73 caracteres → 400 `VALIDATION_FAILED`.
- `./mvnw clean test` → verde x2.
- En vivo (`PORT=4704 ./mvnw -Pfrontend spring-boot:test-run`, solo si el puerto 4704 está libre): `curl -sI localhost:4704/ | grep -i content-security-policy`; el HTML y sus assets cargan; detener por PID.
- Commit `feat(seguridad): CSP, favicon.svg y límites de entrada (tarea 514)`.

## Resultado
- CSP añadida en `WebSecurityConfig` (política del diseño, sin excepción para Swagger: sus scripts son externos y no la rompen). `/favicon.svg` pública. `@Size(max=72)` en password (Register y Login) y `@Size(max=50)` en name.
- Tests: 3 nuevos en rojo (500 por bcrypt, 401 favicon, sin CSP) y luego verdes. `./mvnw clean test` x2: 106 tests, 0 fallos.
- En vivo (puerto 4704, detenido por PID): GET `/` 200 con `Content-Security-Policy`; assets js/css y `/favicon.svg` 200; `/swagger-ui/index.html` 200 con scripts externos. Nota: `curl -sI` (HEAD) no muestra CSP, GET sí.
- No verificado en navegador: `/v3/api-docs` da 401 (no está en la lista pública; preexistente), así que Swagger UI puede no cargar la spec; y `ws:`/`wss:` para STOMP. Que el chat central lo pruebe.
