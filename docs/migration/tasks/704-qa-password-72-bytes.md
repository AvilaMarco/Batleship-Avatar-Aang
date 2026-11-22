# 704 · qa · Test: contraseña de más de 72 bytes → 400
Estado: hecha
Fase: F7
Depende de: 702
Paralelizable: sí
Origen: tarea 702 (el encoder lanza IllegalArgumentException con > 72 bytes).
Riesgo: bajo

Objetivo: en `AuthIntegrationTest`, registrar con contraseña de 40 caracteres "é" (80 bytes, ≤ 72 caracteres) debe responder 400 `VALIDATION_FAILED` (hoy lo cubre `RegisterRequest.isPasswordWithin72Bytes`; sin él sería 500).

Criterios: test verde con el código actual; rojo si se quita el `@AssertTrue`.

## Resultado
Test `register_withPasswordOver72BytesButUnder72Chars_returns400ValidationFailed` en AuthIntegrationTest (40 x "é" = 80 bytes -> 400 VALIDATION_FAILED).
Mutación (quitar `@AssertTrue`): el test falla con 500; revertido con `git checkout`, sin rastro en src/main. `./mvnw clean test` x2: 112 tests, verde.
Hallazgo: la clave del error es `errors.passwordWithin72Bytes`, no `errors.password` (el front no podrá asociarlo al campo). Fijado tal cual (caracterización); sugerencia a backend-dev: `@AssertTrue` con nodo `password` o mensaje propio.
