# 813 · qa · GameMap: usar assets/map.svg
- Estado: hecha-parcial · Origen: "mapa en SVG redibujado" (usuario vía sesión central) · Riesgo: bajo
- `GameMap.test.tsx` línea 25 espera `<image href="/assets/img/mapa-game-mobile.png">`. Al pasar a `/assets/map.svg` (plano, mismas coordenadas 1400×800) hay que cambiar esa aserción. El componente sigue con el PNG hasta que qa actualice el test.
- Después: en `GameMap.tsx` cambiar el `href` del `<image>` a `/assets/map.svg`.

## Resultado
- Parte de test hecha: `GameMap.test.tsx` espera `/assets/map.svg` (rojo hasta que frontend-dev cambie `GameMap.tsx`, tarea 816).
- Falta (no es de qa): el cambio de `href` en `GameMap.tsx`.
