# 701 · qa · Tests: contraseña mínima de 8 caracteres al registrarse (test primero)
Estado: pendiente
Fase: F7
Depende de: —
Paralelizable: no (todos los tests de integración y el E2E usan contraseñas)
Origen: revisión 606 hallazgo MENOR 4 ("una contraseña de 1 carácter es válida"); usuario: "si. continua" a "¿hago la contraseña mínima de 8 caracteres?".
Riesgo: normal

Objetivo: fijar el contrato "registro exige contraseña de 8 a 72 caracteres" y adaptar los tests que registran con contraseñas cortas.

Archivos: `src/test/java/**` (AuthHelper, AuthIntegrationTest y cualquier test que registre usuarios con `pw123` u otra contraseña de < 8 caracteres), `frontend/e2e/full-game.spec.ts`, `frontend/src/**/*.test.tsx` que usen contraseñas de registro.

Diseño (cerrado):
- Solo el REGISTRO exige mínimo 8 (`RegisterRequest`). El LOGIN no cambia: el seed de dev (`marco@admin.com` / `123`) y cuentas existentes siguen entrando.
- Test nuevo (rojo hasta la 702): registrar con 7 caracteres → 400 `VALIDATION_FAILED` con error en `password`; con exactamente 8 → 201; con 73 → 400 (ya existe).
- Cambiar las contraseñas de registro de los tests y del E2E a ≥ 8 caracteres (p. ej. `pw12345678`), también en el login que las use después. No tocar los logins del seed (`123`).

Criterios de aceptación:
- `./mvnw clean test`: el único rojo nuevo es el test de 7 caracteres (anota la lista exacta de fallos en el Resultado); el resto verde.
- `cd frontend && npm test` verde (si algún test de pantalla cambia, solo la contraseña).
- No ejecutes el E2E (necesita backend y puertos): déjalo actualizado y márcalo `no ejecutado` en el Resultado.
- Commit `test: contraseña mínima de 8 caracteres al registrarse (tarea 701, rojo hasta 702)`.

## Resultado
