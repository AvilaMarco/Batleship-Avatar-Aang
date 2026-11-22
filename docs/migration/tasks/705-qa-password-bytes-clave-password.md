# 705 · qa · Test: el error de más de 72 bytes llega bajo la clave `password`
Estado: hecha
Fase: F7
Depende de: 704
Paralelizable: no
Origen: hallazgo de la tarea 704: el error llega como `errors.passwordWithin72Bytes` ("must be true"), no como `errors.password`; el front no puede asociarlo al campo y muestra un texto en inglés.
Riesgo: bajo

Objetivo: cambiar la expectativa del test de 704 a `errors.password` con un mensaje legible (rojo hasta la 706).

Archivos: `src/test/.../integration/AuthIntegrationTest.java` (excepción: editar SOLO `register_withPasswordOver72BytesButUnder72Chars_returns400ValidationFailed`).

Diseño: 40 x "é" (80 bytes) → 400 `VALIDATION_FAILED`, `errors.password` presente y su primer mensaje contiene "72 bytes" (el texto exacto lo fija la 706); y `errors` NO contiene la clave `passwordWithin72Bytes`.

Criterios: `./mvnw clean test`: el único rojo es ese test; el resto verde. Commit `test: error de bytes bajo la clave password (tarea 705, rojo hasta 706)`.

## Resultado
Test `register_withPasswordOver72BytesButUnder72Chars_returns400ValidationFailed` ahora espera `errors.password[0]` que contenga "72 bytes" y que `errors.passwordWithin72Bytes` no exista. Rojo hasta la 706.

`./mvnw clean test`: 112 tests, 1 fallo (el único), resto verde. Fallo exacto:
`AuthIntegrationTest.register_withPasswordOver72BytesButUnder72Chars_returns400ValidationFailed:86 java.lang.AssertionError: No value at JSON path "$.errors.password[0]"`
Motivo correcto: hoy el error llega bajo `passwordWithin72Bytes`, no bajo `password`.
