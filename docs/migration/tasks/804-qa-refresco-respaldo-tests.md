# 804 · qa · Tests: refresco de respaldo de la partida (rojo hasta 805)
Estado: hecha
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
5 casos añadidos en `GameScreen.test.tsx` (relojes simulados). En rojo (2), ambos porque `GameScreen` aún no tiene temporizador (`getGame` se llama 1 vez, no 2):
- "refresco: con la partida en curso vuelve a pedirla cada 15 s sin eventos".
- "refresco: no solapa peticiones si la anterior sigue en curso" (rojo por la misma causa; se vuelve significativo al implementar 805).
En verde (3, ya cubiertos o guardas de regresión): visibilitychange/online (ya existe), desmontar sin timers ni listeners, FINISH sin refresco periódico.
