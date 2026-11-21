# 601 · devops · Dockerfile multi-stage, .dockerignore y fin de línea LF
Estado: hecha
Fase: F6
Depende de: —
Paralelizable: sí, con 602 (602 toca solo `src/`; esta tarea no ejecuta Maven en el host). El build de Docker usa la misma Docker Desktop que los tests: no correr `./mvnw test` a la vez si la máquina va justa de RAM.
Origen: ADR 007 puntos 1, 2 y 12; pedido del usuario "Dockerfile multi-stage (Node para el front + JDK 21 para build + JRE 21 para run; usuario no root; capas con caché de dependencias; healthcheck…)".
Riesgo: normal (no expone nada; la imagen se revisa en 606)

Objetivo: `docker build` produce una imagen `shipbender` que contiene el front y corre como usuario no root con el perfil `prod`.

Archivos: `Dockerfile`, `.dockerignore`, `.gitattributes` (excepción explícita a tu alcance: solo este archivo, con el contenido del ADR 007 Anexo A).

Diseño (cerrado): copiar tal cual el ADR 007 Anexo A (Dockerfile, lista de `.dockerignore`, líneas de `.gitattributes`). No tocar `pom.xml`.
- Después de commitear `.gitattributes`, reescribir `mvnw` en LF en el disco: `rm mvnw && git checkout -- mvnw` (el índice ya está en LF).

Criterios de aceptación:
- `git ls-files --eol mvnw` → `i/lf w/lf attr/text eol=lf`.
- `docker build -t shipbender:601 .` termina sin errores.
- `docker run --rm --entrypoint id shipbender:601` → uid distinto de 0 (`app`).
- `docker run --rm --entrypoint sh shipbender:601 -c 'unzip -l /app/app.jar | grep -c "BOOT-INF/classes/static/index.html"; unzip -l /app/app.jar | grep -c devtools'` → `1` y `0`.
- `docker run --rm --entrypoint printenv shipbender:601 SPRING_PROFILES_ACTIVE` → `prod`.
- Caché: cambiar un comentario en un `.java`, repetir el build y comprobar que `npm ci` sale `CACHED` y que Maven no vuelve a bajar dependencias (log sin `Downloading`). Revertir el cambio.
- No arrancar la app (no hay BD; eso es 603). Borrar la imagen de prueba: `docker image rm shipbender:601`.
- Si `npm ci` falla por un binario nativo de plataforma que falta en el lock (bug de npm con dependencias opcionales): marcar `bloqueada` y explicar; no regenerar el lock.
- Commit `build: Dockerfile multi-stage y fin de línea LF para scripts (tarea 601)`.

## Resultado
Archivos: `Dockerfile`, `.dockerignore`, `.gitattributes` copiados del ADR 007 Anexo A (sin cambios). `mvnw` reescrito en LF con `rm mvnw && git checkout -- mvnw`. Commit sobre `migration/f6-deploy`.

Comprobaciones (salida real):
- `git ls-files --eol mvnw` → `i/lf    w/lf    attr/text eol=lf      	mvnw` (modo `100755` conservado).
- `docker build -t shipbender:601 .` → `exit=0`, `#25 naming to docker.io/library/shipbender:601`. `npm ci`: `added 106 packages`. Vite: `✓ built in 206ms`. Maven: `BUILD SUCCESS`.
- `docker run --rm --entrypoint id shipbender:601` → `uid=100(app) gid=101(app) groups=101(app)`. `docker image inspect` → User `app`.
- `unzip -l /app/app.jar | grep -c "BOOT-INF/classes/static/index.html"` → `1`. `grep -c devtools` → `0`.
- `printenv SPRING_PROFILES_ACTIVE` → `prod`.
- Caché (en una copia del contexto en scratchpad, para no tocar `src/` de backend-dev): se cambió un comentario en `ChatService.java`. Segundo build: `npm ci` y todas las capas del front `CACHED` (10 líneas CACHED); stage Maven re-ejecutado en 7.6 s con `BUILD SUCCESS`; `Downloading` = 0, `Downloaded from` = 0.
- Limpieza: `docker image rm shipbender:601 shipbender:601-cache` → `exit=0`. Contenedores: solo queda `testcontainers-ryuk` (no tocado). Imágenes base `node:22-slim`, `eclipse-temurin:21-*` y `postgres` se conservan (caché compartida).

Notas:
- No se arrancó la app ni se ejecutó Maven en el host.
- `npm ci` no falló: el lock ya trae `@rolldown/binding-linux-x64-gnu`, `@oxlint/binding-linux-x64-gnu` y `lightningcss-linux-x64-gnu`. No se tocó el lock.
- La imagen de prueba se construyó desde el working tree, que incluía cambios sin commitear de 602 (`SecurityRulesIntegrationTest`, `WebSocketOriginBehindProxyTest`). Esos archivos no se commitearon.
- `git add .gitattributes` avisa: "LF will be replaced by CRLF the next time Git touches it". Es el efecto de `core.autocrlf=true` en el disco; el índice queda en LF.
- No comprobado: el `docker compose` (603), el healthcheck `wget` (solo se usó la imagen, no se levantó el contenedor) y el build en la máquina de servidor (U2).
