# 803 · frontend-dev · Mapa SVG con menú de acciones
Estado: pendiente
Fase: F8
Depende de: 802
Paralelizable: no
Origen: tarea 801; mensaje de la sesión de diseño (REDISENO.md §3).
Riesgo: normal

Objetivo: implementar `GameMap` como SVG (1400×800) con una región accesible por ubicación y un menú de acciones, y cablearlo en `LobbyScreen`, hasta poner en verde todos los tests de 801.

Archivos: `frontend/src/components/GameMap.tsx`, `frontend/src/screens/LobbyScreen.tsx` (solo el cableado), y el asset del mapa: recuperar el original de git a `frontend/public/assets/img/mapa-game-mobile.png` con `git show b40e276^:src/main/resources/static/web/mapa-game-mobile.png > <destino>` (mismo contenido que añadirá la sesión de diseño: así no hay conflicto; si el archivo ya existe con el mismo contenido, no lo toques). NO tocar CSS ni tokens (sesión de diseño): usa estas clases/atributos para que ella estilice: `.map` (contenedor con scroll), `.map-spot` con `data-state="empty|join|mine|full"`, `.map-sheet` (menú). Un estilo mínimo imprescindible para que el menú sea usable (posición debajo del mapa, sin decorar) se permite inline solo si no existe ninguno; no inventes diseño.

Diseño (cerrado): ver 801. Accesibilidad: cada ubicación es `role="button"` con `tabindex="0"`, nombre `"<id>: <estado en español>"` y responde a Enter/Espacio; el menú anuncia su apertura (`role="dialog"` con nombre o región con `aria-live`) y devuelve el foco a la ubicación al cerrarse. El estado nunca depende solo del color (la sesión de diseño añade glifos/números; tú expones el `data-state` y el texto en el nombre accesible).

Criterios de aceptación:
- `cd frontend && npm test && npm run build` → exit 0, todos los tests de 801 en verde.
- `grep -rn "dangerouslySetInnerHTML" frontend/src` → sin salida.
- Prueba manual en el navegador (backend en 4704 y Vite en 4705; la hace el chat central): el mapa se ve completo y los puntos caen sobre sus ubicaciones; tocar una ubicación abre el menú; crear/unirse/volver funcionan; al entrar el mapa queda centrado en la nación del jugador.
- Commit `feat(front): mapa SVG con menú de acciones (tarea 803)`.

## Resultado
