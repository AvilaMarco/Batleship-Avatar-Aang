# 510 · backend-dev · Front empaquetado en el jar y rutas públicas nuevas
Estado: hecha
Fase: F5
Depende de: 509
Paralelizable: sí, con 502-507 (solo pom y `security/`; no usa puertos)
Origen: ADR 001 punto 3 ("compilado dentro del jar"); ADR 006 punto 7.
Riesgo: alto (configuración de seguridad) → revisión en 512

Objetivo: `./mvnw -Pfrontend package` produce un jar que sirve el front en `/`; `./mvnw test` sigue sin Node; rutas públicas según 509.

Archivos: `pom.xml`, `src/main/java/dev/racnar/shipbender/security/WebSecurityConfig.java`.

Diseño (cerrado):
- Perfil `frontend` (no activo por defecto) con `com.github.eirslett:frontend-maven-plugin` (última estable): `install-node-and-npm` (Node 22 LTS), `npm ci`, `npm run build` con `workingDirectory` `frontend`, en `generate-resources`. En el mismo perfil, `<resource>` que copia `frontend/dist` a `static/` (`targetPath`), manteniendo `src/main/resources`.
- Ignorar en git lo que instale el plugin (`frontend/node/`) en `.gitignore`.
- Borrar la dependencia `org.webjars:stomp-websocket` (la usaba solo el front viejo).
- `WebSecurityConfig.java:38-39`: lista pública estática = `"/", "/index.html", "/favicon.ico", "/assets/**"`; se quitan `/main.ogg`, `/code/**`, `/img/**`, `/libraries/**`, `/web/**`, `/webjars/**`. Nada más cambia.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 0 fallos (incluye los de 509).
- `./mvnw -q -Pfrontend -DskipTests package && jar tf target/*.jar | grep -E "static/index.html|static/assets/.*\.js"` → ambas líneas.
- `grep -n "webjars" pom.xml src/main/java -r` → sin salida.
- Commit `build: empaquetar el front en el jar con perfil frontend (tarea 510)`.

## Resultado
- `pom.xml`: perfil `frontend` (inactivo por defecto) con frontend-maven-plugin 2.0.2 (Central: release 2.0.2), Node v22.22.2 (22.12 dejaba EBADENGINE con las deps de vite), `npm ci` + `npm run build` en `generate-resources`, y resource `frontend/dist` -> `static/`. Eliminada `org.webjars:stomp-websocket` (queda `webjars-locator-lite` transitivo de springdoc; `grep webjars pom.xml src/main/java` vacío).
- `WebSecurityConfig`: públicas `"/", "/index.html", "/favicon.ico", "/assets/**"`. `.gitignore`: `frontend/node/`.
- `/docs/**` se deja: es el path del OpenAPI JSON (`springdoc.api-docs.path=/docs`) que el Swagger UI de dev necesita; en prod springdoc está desactivado por perfil (404).
- SPA con rutas por hash: no hace falta forward a index.html.
- Verificación: `./mvnw clean test` verde x2 (103 tests, 0 fallos); sin Node instalado en `frontend/node` tras test; `-Pfrontend -DskipTests package` -> jar con `static/index.html` y `static/assets/index-*.js`; en vivo (test-run :4704): `/` 200, `/assets/*.js` y `.css` 200, `/web/games.html` 401, `/api/games` 401, `/swagger` 302.
- Nota: cambiar la versión de Node con `frontend/node/` ya instalado deja npm roto (mezcla de versiones): borrar `frontend/node/`.
