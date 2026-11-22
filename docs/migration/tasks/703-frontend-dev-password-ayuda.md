# 703 · frontend-dev · Registro: indicar el mínimo de 8 caracteres
Estado: hecha
Fase: F7
Depende de: 702
Paralelizable: no
Origen: tarea 701/702; accesibilidad y claridad del formulario.
Riesgo: normal

Objetivo: que el usuario sepa del mínimo antes de enviar y vea el error del servidor.

Archivos: `frontend/src/screens/LoginScreen.tsx`, `LoginScreen.test.tsx`.

Diseño (cerrado): en modo "Registrarse", el campo contraseña lleva `minLength={8}`, `maxLength={72}` y un texto de ayuda visible asociado (`aria-describedby`) "Mínimo 8 caracteres"; el error `VALIDATION_FAILED` del campo `password` se muestra junto al campo (ya existe el mapeo de errores por campo). En modo "Entrar" no hay mínimo.

Criterios de aceptación (tests primero):
- Test: en registro aparece el texto de ayuda y el input tiene `minLength=8`; en "Entrar" no.
- `cd frontend && npm test && npm run build` → exit 0.
- Commit `feat(front): indicar el mínimo de 8 caracteres en el registro (tarea 703)`.

## Resultado
Registro: contraseña con `minLength=8`, `maxLength=72` y ayuda "Mínimo 8 caracteres" enlazada con `aria-describedby`; en "Entrar" nada de eso. El error `VALIDATION_FAILED` llega en `errors.password` (array, igual para longitud o >72 bytes) y se muestra en el `role="alert"` con el nombre en español ("Contraseña: ..."). 3 tests nuevos (rojo verificado: 2 fallaban). `npm test && npm run build` exit 0.
