# 802 · frontend-dev · Lógica pura del mapa y constantes
Estado: hecha
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
`domain/map.ts` (`locationState`, `initialCenter`) y `MAP_W`/`MAP_H` (1400x800) en `map-locations.ts`. `map.test.ts` en verde (8 tests); GameMap/Lobby siguen en rojo (15 fallos, esperado hasta 803). Para que `npm run build` pase (el test de GameMap pasa `nation`), se añadió `nation?` opcional a las Props de `GameMap.tsx`; la 803 lo usa. Grep de pureza sin salida.
