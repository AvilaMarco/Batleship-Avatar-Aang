# 702 · backend-dev · Registro: contraseña de 8 a 72 caracteres
Estado: pendiente
Fase: F7
Depende de: 701
Paralelizable: no
Origen: revisión 606 hallazgo MENOR 4; tarea 701.
Riesgo: normal

Objetivo: poner en verde el test de 701.

Archivos: `src/main/java/dev/racnar/shipbender/player/dto/RegisterRequest.java`.

Diseño (cerrado): `@Size(min = 8, max = 72)` en `password` (el máximo ya existe). `LoginRequest` NO cambia (el seed y las cuentas existentes tienen contraseñas cortas). Nota para el Resultado: `@Size` cuenta caracteres, no bytes (bcrypt corta en 72 BYTES): con caracteres no ASCII una contraseña de ≤ 72 caracteres puede superar los 72 bytes; si el encoder de la versión actual lanza error con > 72 bytes, valida también en bytes (`password.getBytes(UTF_8).length <= 72`) con una restricción propia y un test, y dilo en el Resultado.

Criterios de aceptación:
- `./mvnw clean test` verde x2 (todos, incluido el de 701).
- Si implementaste la validación en bytes: un test con 40 caracteres de 2 bytes (80 bytes) → 400.
- Commit `feat(seguridad): contraseña mínima de 8 caracteres al registrarse (tarea 702)`.

## Resultado
