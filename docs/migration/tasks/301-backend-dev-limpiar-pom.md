# 301 · backend-dev · Limpiar pom.xml antes de los saltos
Estado: hecha
Fase: F3
Depende de: — (F2 mergeada)

Objetivo: sacar del `pom.xml` lo que estorba en los saltos de versión, sin cambiar todavía la versión de Boot (sigue en 2.6.6). Ver ADR 003.

Archivos: `pom.xml`.

Cambios (decididos):
- Borrar la dependencia `mysql:mysql-connector-java`: no hay configuración MySQL en el proyecto y en Boot 3 cambia de coordenadas.
- Borrar el plugin `org.springdoc:springdoc-openapi-maven-plugin` entero.
- En `spring-boot-maven-plugin`: borrar `<version>2.6.7</version>` (que la versión venga del parent) y borrar todo el bloque `<executions>` (start/stop en `integration-test`). Queda solo `groupId` + `artifactId`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `grep -n "mysql\|springdoc-openapi-maven-plugin\|<executions>\|2.6.7" pom.xml` → sin resultados.
- `./mvnw -q -DskipTests verify` → termina sin levantar la app (no aparece el banner de Spring en la salida) y sin errores.
- `PORT=4704 ./mvnw spring-boot:run` → arranca; `curl -s -o /dev/null -w "%{http_code}" http://localhost:4704/swagger-ui/index.html` → `200`. Detén el proceso al terminar.

## Resultado
Estado: hecha

Se borraron del `pom.xml` el conector MySQL, el plugin `springdoc-openapi-maven-plugin` y, en `spring-boot-maven-plugin`, la versión 2.6.7 y el bloque `<executions>`.
- `./mvnw test`: BUILD SUCCESS, 30 tests, 0 fallos.
- El grep del criterio no devuelve resultados.
- `./mvnw -q -DskipTests verify`: sin errores (salida vacía en modo -q, así que no hay banner de Spring).
- `PORT=4704 ./mvnw spring-boot:run`: swagger-ui/index.html devolvió 200. El proceso se detuvo y el puerto 4704 quedó libre, sin java corriendo.
