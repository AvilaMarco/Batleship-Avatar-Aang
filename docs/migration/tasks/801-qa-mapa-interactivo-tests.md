# 801 · qa · Tests del mapa interactivo (rojo hasta 802-803)
Estado: hecha
Fase: F8
Depende de: —
Paralelizable: no (toca LobbyScreen.test.tsx, GameMap y el E2E)
Origen: usuario (2026-11-22): "Mapa: arreglo + SVG con menú"; mensaje de la sesión de diseño (docs/design/REDISENO.md §3 en `claude/diseno-visual-bc407b`): el mapa se dibuja con un SVG de viewBox 1400×800 (la lógica es de la sesión principal; los estilos, de la de diseño). Bug confirmado: `GameMap.tsx` usa `fondo-mapa.png` (1395×950, el marco del menú) con coordenadas del mapa real `mapa-game-mobile.png` (1400×800).
Riesgo: normal

Objetivo: fijar el comportamiento nuevo del mapa con tests (en rojo hasta que lo implementen 802 y 803).

Archivos: `frontend/src/domain/map.test.ts` (nuevo), `frontend/src/screens/LobbyScreen.test.tsx` y un `frontend/src/components/GameMap.test.tsx` (nuevo), `frontend/e2e/full-game.spec.ts`. NO tocar estilos (CSS): son de la sesión de diseño.

Diseño del comportamiento (cerrado):
- Constantes `MAP_W = 1400`, `MAP_H = 800` en `domain/map-locations.ts`; imagen del mapa `/assets/img/mapa-game-mobile.png`.
- `domain/map.ts` (TS puro, sin React): `locationState(loc, games, myId): { state: "empty" | "join" | "mine" | "full"; game?: GameSummary }`: sin partida en `location` → `empty`; con partida de 1 jugador que no soy yo → `join`; partida donde está `myId` → `mine`; partida con 2 jugadores donde no estoy → `full`. `initialCenter(nation, locations): {x, y}`: centro (media) de las ubicaciones de esa nación; sin nación (`null`) → centro del mapa.
- `GameMap` (SVG): un `<svg viewBox="0 0 1400 800">` con `<image>` y un `<g role="button" tabindex="0" aria-label="<id>: <estado>" data-state=...>` por ubicación con las coordenadas de `map-locations.ts`; clic o Enter/Espacio en una ubicación NO crea nada: abre un menú HTML (`role="dialog"`/región con nombre) con las acciones de esa ubicación: `empty` → botón "Crear partida en <id>"; `join` → "Unirse a la partida en <id>"; `mine` → "Volver a la partida en <id>"; `full` → texto "Partida llena" sin acción (botón deshabilitado). Escape o un botón "Cerrar" cierran el menú. Las acciones llaman a los mismos callbacks que hoy (crear con `{nation: loc.nation, location: loc.id}`, unirse, volver) y los errores `LOCATION_TAKEN`/`GAME_FULL`/`ALREADY_IN_GAME` siguen mostrándose y recargando la lista.
- Contenedor con desplazamiento (`overflow: auto`) cuya posición inicial centra `initialCenter(profile.nation)` (comprobable en test con las propiedades `scrollLeft`/`scrollTop` fijadas por el componente).

Casos de test (cerrados): `locationState` los 4 estados + `myId` en partida de 2; `initialCenter` con y sin nación; GameMap: el `viewBox` es `0 0 1400 800` y la imagen es el mapa; hay un control por ubicación con `data-state` correcto; tocar una ubicación libre abre el menú y NO llama a crear hasta pulsar la acción; "Crear partida en E4" llama a crear con `{nation: "EARTH", location: "E4"}`; con partida ajena de 1 jugador el menú ofrece "Unirse a la partida en <id>" y navega a `#/games/7`; con partida propia ofrece "Volver"; con partida llena no hay acción; Escape cierra el menú; el control responde a Enter y Espacio. Actualiza los tests de LobbyScreen que fijaban el comportamiento viejo (un botón directo por ubicación) y el E2E (dos pasos: pulsar la ubicación y luego la acción; localizar por rol y nombre accesible, sin esperas fijas).

Criterios de aceptación:
- `cd frontend && npm test`: fallan SOLO los tests nuevos/actualizados del mapa (anota la lista exacta en el Resultado); el resto verde.
- NO ejecutes el E2E (necesita backend y puertos): déjalo actualizado y márcalo `no ejecutado`.
- Commit `test: mapa interactivo con SVG y menú (tarea 801, rojo hasta 803)`.

## Resultado

Tests: `domain/map.test.ts` (locationState x5, initialCenter x2, constantes), `components/GameMap.test.tsx` (10 casos), `LobbyScreen.test.tsx` (4 actualizados a dos pasos), `e2e/full-game.spec.ts` (dos pasos; **no ejecutado**).
Contrato asumido para 802/803: `GameMap` recibe la prop nueva `nation: Nation | null`; `locationState(id: string, games, myId)` (primer arg = id de ubicación) devuelve `{state}` sin la clave `game` cuando es `empty`; control = `role=button` con `aria-label="<id>: <estado>"` (estado = `empty|join|mine|full`, igual que `data-state`; el E2E usa `W1: empty`-style); `<image href>`; scroll comprobado comparando `scrollLeft` de FIRE < EARTH en los ancestros del svg.

`cd frontend && npm test`: 15 rojos, 69 verdes (3 ficheros rojos, 12 verdes).
- `map.test.ts` (fichero entero): `Failed to resolve import "./map" from "src/domain/map.test.ts"` (domain/map.ts no existe).
- `GameMap.test.tsx` (10): Escape cierra, svg 1400x800, Cerrar cierra, contenedor centrado (`Cannot read properties of null (reading 'parentElement')`: no hay svg), Enter, Espacio, data-state, Unirse, partida llena, Volver, abrir menú sin crear; fallos por `Unable to find an accessible element with the role "button" and name /^E1:/` (no existe el control del mapa nuevo).
- `LobbyScreen.test.tsx` (4): LOCATION_TAKEN, Unirse y navega, Volver, punto libre crea; `Unable to find role="button" and name /^E1:/` (idem).
