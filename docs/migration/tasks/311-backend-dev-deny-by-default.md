# 311 · backend-dev · Seguridad HTTP deny-by-default
Estado: pendiente
Fase: F3
Depende de: 310

Objetivo: hoy cualquier ruta que no esté en la lista pasa por `anyRequest().permitAll()`. Se cambia a `denyAll()` con una lista explícita de rutas públicas. El front viejo y los 30 tests tienen que seguir funcionando. Ver ADR 003 punto 6.

Archivos: `config/WebSecurityConfig.java`, `application-prod.properties`.

Reglas (cerradas; el orden importa, gana la primera que coincide):
1. `POST /api/players/login`, `POST /api/players` (registro) → `permitAll`.
2. `/api/players/nation/**`, `/api/match/**` → `hasAuthority("PLAYER")`.
3. `/api/games/**` → `hasAnyAuthority("GUEST", "PLAYER")`.
4. Front viejo (el JWT vive en localStorage; al navegar a una página no se manda header): `/`, `/index.html`, `/favicon.ico`, `/main.ogg`, `/code/**`, `/img/**`, `/libraries/**`, `/web/**`, `/webjars/**` → `permitAll`.
5. Handshake `/the-last-shipbender/**` → `permitAll`. La autenticación se hace en el CONNECT de STOMP (305); el navegador no puede mandar `Authorization` en el handshake.
6. `/swagger`, `/swagger-ui/**`, `/docs/**` → `permitAll`. En `application-prod.properties`: `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false`.
7. `/error` → `permitAll` (desde Security 6 también se autoriza el dispatch de error; si no, un 404 o 500 llega como 403).
8. `anyRequest().denyAll()`.
- Se borran los matchers que no son rutas HTTP o no existen: `/topic/**`, `/app/**` (son destinos STOMP), `/web/games`, `/users/admin`, `/configuration/**`, `/swagger-resources/**`, `/swagger-ui.html`.
- El logout `/api/players/logout` no cambia (lo atiende el `LogoutFilter` antes de autorizar).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `grep -n "anyRequest" src/main/java/com/codeblockacademy/shipbender/config/WebSecurityConfig.java` → solo `anyRequest().denyAll()`.
- Con `PORT=4704 ./mvnw spring-boot:test-run`, `curl -s -o /dev/null -w "%{http_code}\n"`:
  - `/web/games.html`, `/`, `/swagger-ui/index.html`, `/webjars/stomp-websocket/2.3.4/stomp.min.js` → `200`;
  - `/api/games` sin token → `403`; `/api/inventado` sin token → `403`; `/the-last-shipbender` → distinto de `403`.
- La prueba manual de la 307 sigue funcionando. Detén el proceso.

## Resultado
