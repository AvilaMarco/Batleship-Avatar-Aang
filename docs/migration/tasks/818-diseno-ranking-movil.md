# 818 · diseño · La tabla del ranking desborda en móvil (390 px)
Estado: hecha
Fase: F8
Origen: revisión visual del chat central (2026-11-22): a 390x844 el ranking del lobby mide ~502 px y ensancha toda la página (el viewport pasa a 518 px); a 844x390 y 1280 está bien.
Riesgo: bajo (solo CSS/maquetación; sesión de diseño, dueña de lo visual)

Arreglo esperado: que `Ranking` quepa en 390 px (contenedor con `overflow-x:auto` o columnas compactas/abreviadas con `abbr`/`aria-label`), sin scroll horizontal de la página. Comprobar: a 390 `document.documentElement.scrollWidth <= innerWidth` en `#/`. Sin cambiar lógica ni tests existentes.

## Resultado
Hecho: CSS en `screens.css` (sección `min-width:0; overflow-x:auto` + celdas compactas a <=480 px). Medido a 390: scrollWidth 523 -> 390. npm test 134 y build verdes.
