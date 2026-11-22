# 805 · frontend-dev · Refresco de respaldo de la partida
Estado: hecha
Fase: F8
Depende de: 804
Paralelizable: no
Origen: tarea 804.
Riesgo: normal

Objetivo: poner en verde los tests de 804.

Archivos: `frontend/src/screens/GameScreen.tsx` (y, si ayuda a mantenerlo limpio, un hook `useGameRefresh` en `frontend/src/` sin estilos).

Diseño (cerrado): un intervalo de 15 s que llama a la misma función de recarga que usan los eventos STOMP; una guarda "ya hay una petición en curso"; listeners de `visibilitychange` y `online`; todo se limpia al desmontar; sin refresco con la partida `FINISH`. No cambiar el protocolo STOMP ni el contrato.

Criterios: `cd frontend && npm test && npm run build` exit 0; commit `feat(front): refresco de respaldo de la partida (tarea 805)`.

## Resultado
`GameScreen.tsx`: `setInterval` de 15 s que llama a `load` con guarda `inFlight` (sin solape), omitido con `status === "FINISH"`, limpiado al desmontar. visibilitychange/online ya existían. Sin CSS.
