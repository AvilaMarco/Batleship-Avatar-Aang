# 304 · backend-dev · Compilar con Java 21
Estado: pendiente
Fase: F3
Depende de: 303

Objetivo: compilar y ejecutar con Java 21. Va después de Boot 3.5 porque el byte-buddy (Mockito) de Boot 2.7 no lee class files de Java 21. Ver ADR 003.

Archivos: `pom.xml`.

Cambios: `<java.version>21</java.version>`. Nada más (no reescribas código para usar features de Java 21; eso es F4).

Criterios de aceptación:
- `./mvnw -v` muestra Java 21.
- `./mvnw clean test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `javap -v -cp target/classes com.codeblockacademy.shipbender.SalvoApplication | grep "major version"` → `major version: 65`.
- `PORT=4704 ./mvnw spring-boot:run` arranca. Detén el proceso.

## Resultado
