# 101 · backend-dev · Build reproducible con el wrapper y JDK 21
Estado: pendiente
Fase: F1
Depende de: —

Objetivo: que `./mvnw test` funcione en un clon limpio con el JDK 21 instalado, sin tocar código Java. Hoy la línea base no se puede ejecutar:
1. `./mvnw` falla: falta `.mvn/wrapper/maven-wrapper.properties` (nunca se commiteó; `.gitignore` solo ignora el `.jar`).
2. Con Maven 3.9.16 a mano, la compilación falla en Lombok: `NoSuchFieldError: JCTree$JCImport ... qualid`. Boot 2.6.6 trae Lombok 1.18.22, que no soporta JDK 21.
   Probado: con `-Dlombok.version=1.18.36` compila y pasan 3 tests (BUILD SUCCESS).

Archivos:
- `.mvn/wrapper/maven-wrapper.properties` (nuevo; también puede regenerar `mvnw` y `mvnw.cmd`)
- `pom.xml`: solo agregar `<lombok.version>1.18.36</lombok.version>` en `<properties>`

Pasos:
1. Generar el wrapper con la Maven en caché (`~/.m2/wrapper/dists/apache-maven-3.9.16-bin/*/apache-maven-3.9.16/bin/mvn`):
   `mvn -N wrapper:wrapper -Dmaven=3.9.16`
2. Agregar la propiedad `lombok.version` al `pom.xml`. No cambiar `java.version` (17) ni ninguna otra dependencia.

Criterios de aceptación:
- `./mvnw -v` muestra `Apache Maven 3.9.16` y Java 21.
- `./mvnw test` → `BUILD SUCCESS`, `Tests run: 3, Failures: 0, Errors: 0`.
- `git status --short` muestra solo `pom.xml`, `.mvn/wrapper/maven-wrapper.properties` y, si cambiaron, `mvnw`/`mvnw.cmd`.
- `git diff pom.xml` muestra solo la línea de `lombok.version`.

## Resultado
