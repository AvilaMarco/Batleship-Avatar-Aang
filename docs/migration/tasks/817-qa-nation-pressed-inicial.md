# 817 · qa · Test LoginScreen.nation: getAllByRole lanza con 0 coincidencias
- Estado: hecha · Origen: tarea 815/812 (selector de nación) · Riesgo: bajo
- `LoginScreen.nation.test.tsx` (3er caso, línea 33): `expect(within(c).getAllByRole("button", { pressed: true })).toHaveLength(0)` lanza antes de comparar, porque `getAllBy*` falla si no hay coincidencias. El spec de 815 pide "ninguno pulsado inicialmente", que es lo implementado.
- Arreglo: usar `queryAllByRole("button", { pressed: true })` en esa línea (y en la final no hace falta). Es el único test rojo de `npm test`.

## Resultado
Linea 33 usa queryAllByRole. npm test 134/134 verde, npm run build ok.
