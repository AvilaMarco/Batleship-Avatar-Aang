# 802 · frontend-dev · Lógica pura del mapa y constantes
Estado: pendiente
Fase: F8
Depende de: 801
Paralelizable: no
Origen: tarea 801.
Riesgo: normal

Objetivo: poner en verde `domain/map.test.ts`.

Archivos: `frontend/src/domain/map.ts` (nuevo), `frontend/src/domain/map-locations.ts` (constantes `MAP_W`/`MAP_H`).

Diseño: ver 801 (`locationState`, `initialCenter`). Sin React ni red en `domain/` (el grep de pureza de la 502 debe seguir sin salida).

Criterios: `cd frontend && npm test` → los tests de `map.test.ts` en verde, sin romper los demás (los de GameMap/Lobby siguen en rojo hasta 803); `npm run build` exit 0; commit `feat(front): lógica pura del mapa (tarea 802)`.

## Resultado
