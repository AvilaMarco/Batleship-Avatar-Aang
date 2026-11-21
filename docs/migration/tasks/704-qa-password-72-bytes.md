# 704 · qa · Test: contraseña de más de 72 bytes → 400
Estado: pendiente
Fase: F7
Depende de: 702
Paralelizable: sí
Origen: tarea 702 (el encoder lanza IllegalArgumentException con > 72 bytes).
Riesgo: bajo

Objetivo: en `AuthIntegrationTest`, registrar con contraseña de 40 caracteres "é" (80 bytes, ≤ 72 caracteres) debe responder 400 `VALIDATION_FAILED` (hoy lo cubre `RegisterRequest.isPasswordWithin72Bytes`; sin él sería 500).

Criterios: test verde con el código actual; rojo si se quita el `@AssertTrue`.

## Resultado
