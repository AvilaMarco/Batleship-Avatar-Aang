# 812 · qa · e2e: el selector de ubicación libre usa la etiqueta vieja
Estado: pendiente
Fase: F8
Depende de: —
Paralelizable: sí (solo `frontend/e2e`)
Origen: usuario: "procede con todas las tareas pendientes"; `npm run e2e` falla en `full-game.spec.ts:43` (`/: empty$/`) porque desde 3dcd5c3 las etiquetas del mapa están en español ("libre", "con un jugador", "tu partida", "llena").
Riesgo: bajo

Objetivo: dejar `npm run e2e` en verde (backend en 4704 y Vite en 4705 ya levantados; no los reinicies ni uses otros puertos). Cambiar el selector a `/: libre$/` y, si tras ello falla otro paso por la UI nueva (botón "Listo · enviar barcos", colocar flota, login sin heading, menú del lobby), adaptar solo ese selector sin debilitar lo que comprueba el test. Si el fallo es un bug real de la app, no lo ocultes: repórtalo.

Criterios: `cd frontend && npm run e2e` → exit 0; `npm test` verde. Commit `test: e2e con las etiquetas en español del mapa (tarea 812)` terminado con la línea Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado
