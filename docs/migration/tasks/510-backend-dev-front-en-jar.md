# 510 · backend-dev · Front empaquetado en el jar y rutas públicas nuevas
Estado: pendiente
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
