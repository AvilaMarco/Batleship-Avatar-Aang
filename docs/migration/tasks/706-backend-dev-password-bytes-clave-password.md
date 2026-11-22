# 706 · backend-dev · Restricción propia de bytes para la contraseña
Estado: hecha
Fase: F7
Depende de: 705
Paralelizable: no
Origen: tareas 704 y 705.
Riesgo: bajo

Objetivo: que el límite de 72 bytes sea una restricción del CAMPO `password`, con mensaje legible.

Archivos: `src/main/java/dev/racnar/shipbender/player/dto/RegisterRequest.java` y una anotación nueva `MaxUtf8Bytes` (+ su `ConstraintValidator`) en `shared/validation/`.

Diseño (cerrado): `@MaxUtf8Bytes(value = 72, message = "no puede superar 72 bytes en UTF-8 (los caracteres no ASCII ocupan más de uno)")` sobre `password`; borrar el método `@AssertTrue isPasswordWithin72Bytes`. El validador acepta `null`.

Criterios: `./mvnw clean test` verde x2 (incluido el test de 705). Commit `refactor(seguridad): restricción MaxUtf8Bytes sobre el campo password (tarea 706)`.

## Resultado
Creada `shared/validation/MaxUtf8Bytes` (anotación + validador interno, acepta null) y aplicada a `password` en `RegisterRequest`; eliminado `isPasswordWithin72Bytes`. El error llega ahora en `errors.password`. `./mvnw clean test` x2: 112 tests, 0 fallos, BUILD SUCCESS.
