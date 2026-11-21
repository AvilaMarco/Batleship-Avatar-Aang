# 513 · frontend-dev · STOMP: releer el token al reconectar y cortar si el CONNECT es rechazado
Estado: pendiente
Fase: F5
Depende de: 512
Paralelizable: sí (solo `frontend/src/api/stomp.ts` y su test)
Origen: hallazgo MENOR 1 de la tarea 512.
Riesgo: normal

Objetivo: con un token caducado el cliente no debe reintentar el CONNECT indefinidamente ni dejar la pantalla colgada.

Archivos: `frontend/src/api/stomp.ts`, `frontend/src/api/stomp.test.ts`.

Diseño (cerrado):
- `beforeConnect` relee `getToken()` y actualiza `connectHeaders.Authorization` en cada intento.
- `onStompError` (frame ERROR de CONNECT) y `onWebSocketClose` tras un CONNECT rechazado: `deactivate()`, `clearToken()` y `navigate("#/login")`, sin reintentos. Una caída de red normal (sin frame ERROR) sigue reintentando cada 5 s.
- El token nunca en logs ni mensajes.

Criterios de aceptación (tests primero, `Client` mockeado):
- Test: `beforeConnect` usa el token actual (cambiado entre intentos); un `onStompError` limpia el token, desactiva el cliente y deja `#/login`; un cierre sin ERROR no limpia nada.
- `cd frontend && npm test && npm run build` → exit 0; `grep -rn "console\." frontend/src/api | grep -v test` → sin salida.
- Commit `fix(front): STOMP relee el token y corta si el CONNECT es rechazado (tarea 513)`.

## Resultado
