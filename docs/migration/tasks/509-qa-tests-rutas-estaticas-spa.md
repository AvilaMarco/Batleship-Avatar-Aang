# 509 · qa · Tests de rutas públicas para el front nuevo (rojo hasta 510)
Estado: hecha
Fase: F5
Depende de: 508
Paralelizable: sí, con 502-507
Origen: ADR 006 punto 7 (front servido desde el jar); ADR 005 punto 10 (test primero); `SecurityRulesIntegrationTest.java:29` fija rutas del front viejo.
Riesgo: alto (reglas de seguridad) → revisión en 512

Objetivo: fijar la nueva lista cerrada de rutas públicas estáticas.

Archivos: `src/test/java/dev/racnar/shipbender/integration/SecurityRulesIntegrationTest.java`.

Diseño (cerrado):
- `publicPaths_withoutToken_areNotForbidden`: `"/"`, `"/index.html"`, `"/assets/index-abc.js"`, `"/favicon.ico"`, `"/swagger-ui/index.html"` (no 403; 404 vale porque en tests no hay build del front).
- `unlistedPaths_areForbidden_evenWithPlayerToken`: agregar `"/web/games.html"`, `"/webjars/stomp-websocket/2.3.4/stomp.min.js"`, `"/libraries/x.js"` (rutas del front viejo que dejan de ser públicas).

Criterios de aceptación:
- `./mvnw test -Dtest=SecurityRulesIntegrationTest` → falla solo por `/assets/...` (aún no permitido) y por las 3 rutas viejas (aún públicas); el resto verde. Anotar los fallos exactos en Resultado.
- Commit `test: rutas públicas del front nuevo (tarea 509, rojo hasta 510)`.

## Resultado
Tests en `SecurityRulesIntegrationTest`: `publicPaths_withoutToken_areNotForbidden` con `/`, `/index.html`, `/assets/index-abc.js`, `/favicon.ico`, `/swagger-ui/index.html`; `unlistedPaths_areForbidden_evenWithPlayerToken` con `/web/games.html`, `/webjars/stomp-websocket/2.3.4/stomp.min.js`, `/libraries/x.js` ademas de los previos.

Ajuste al diseño: sin token una ruta no permitida responde 401 (anonimo), no 403, asi que `assertNotEquals(403)` solo habria dejado `/assets/...` en verde. Se agrego `assertNotEquals(401)` para que falle de verdad hasta la 510.

`./mvnw test -Dtest=SecurityRulesIntegrationTest` -> 13 tests, 4 fallos (esperados, los arregla la 510):
- `publicPaths_withoutToken_areNotForbidden[3]` `/assets/index-abc.js`: era 401, se espera distinto de 401.
- `unlistedPaths_areForbidden_evenWithPlayerToken[3]` `/web/games.html`: esperado 403, fue 200 (probable `target/classes` viejo con static/; con `clean` seria 404, igual rojo).
- `[4]` `/webjars/stomp-websocket/2.3.4/stomp.min.js`: esperado 403, fue 200 (webjar aun en classpath).
- `[5]` `/libraries/x.js`: esperado 403, fue 404.
Resto verde.

Notas de seguridad (contrato sin cambiar):
- `/docs/**` sigue publico en `WebSecurityConfig.java:42`; no esta en la lista cerrada de la tarea, revisar en 510/512 (se espera `/swagger`, `/swagger-ui/**`, `/v3/api-docs`).
- La 510 debe usar `/assets/**` (no `/**`), y `/favicon.ico`, `/index.html`, `/` exactos. Rutas SPA profundas (p. ej. `/games`) necesitaran forward a index.html y no estan cubiertas por estos tests.
- Si se elimina `/webjars/**` el WebSocket/STOMP del front nuevo no depende de el; verificar que `/the-last-shipbender/**` siga publico.
