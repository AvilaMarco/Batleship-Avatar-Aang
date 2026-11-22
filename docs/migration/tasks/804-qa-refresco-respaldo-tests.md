# 804 · qa · Tests: refresco de respaldo de la partida (rojo hasta 805)
Estado: pendiente
Fase: F8
Depende de: 803
Paralelizable: no
Origen: usuario (2026-11-22): "Refresco de respaldo de la partida". Si se pierde un evento del WebSocket la pantalla queda desactualizada hasta el siguiente (ocurrió una vez en una medición, sin causa conocida); el cliente solo vuelve a pedir la partida al conectar y ante eventos (ADR 006 punto 3).
Riesgo: normal

Objetivo: fijar que `GameScreen` se resincroniza por sí sola aunque no llegue ningún evento.

Archivos: `frontend/src/screens/GameScreen.test.tsx` (solo AÑADIR casos).

Casos (con relojes simulados `vi.useFakeTimers`, `api/games` y `api/stomp` mockeados): mientras la partida está visible y no `FINISH`, cada 15 s se vuelve a pedir `getGame`; no se solapan peticiones (si una sigue en curso, el siguiente tick no lanza otra); se vuelve a pedir al recuperar visibilidad (`visibilitychange` a visible) y al evento `online`; al desmontar no quedan temporizadores ni listeners; con la partida `FINISH` no hay refresco periódico.
Criterios: `npm test`: fallan solo los casos nuevos (lista exacta en el Resultado); commit `test: refresco de respaldo de la partida (tarea 804, rojo hasta 805)`.

## Resultado
