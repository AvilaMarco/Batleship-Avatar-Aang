# 601 · devops · Dockerfile multi-stage, .dockerignore y fin de línea LF
Estado: pendiente
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
