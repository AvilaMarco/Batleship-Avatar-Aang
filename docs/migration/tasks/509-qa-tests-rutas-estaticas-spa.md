# 509 · qa · Tests de rutas públicas para el front nuevo (rojo hasta 510)
Estado: pendiente
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
