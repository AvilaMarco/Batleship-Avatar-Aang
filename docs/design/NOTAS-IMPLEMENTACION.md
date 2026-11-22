# Notas de implementación (para la sesión principal)

Diseños: artifact https://claude.ai/artifact/KBrS8Wx75qZX6acFNe863E. Lo visual ya está en el repo (tokens, sprite, manifest, iconos); aquí solo lo funcional.

## Tipografía
Títulos con pincel (caligrafía): `Yuji Boku` + `Kaushan Script` (Google Fonts; ya en `index.html` y `--font-title`). Cuerpo `system-ui`.

## Login
- Sin texto ni puntos. Solo silueta de nación a pantalla completa + audio (`assets/login/intro.ogg`), sincronizada con pistas VTT (`<track kind="metadata">`, cues estimados 4.3/7.7/9.5/12.5 s = agua/tierra/fuego/aire, a confirmar de oído).
- Escritorio: fila de 4 miniaturas bajo el formulario; la de la nación activa va a color con aro, las demás en gris. Mismo evento que cambia la silueta.
- Autoplay bloqueado: arrancar el audio al primer toque; botón silenciar (`aria-pressed`).

## Mapa (pantalla completa)
- El mapa ocupa toda la pantalla, relación 1400×800 conservada (escala = cover), fondo de pergamino (`assets/textures/textura-mapa.jpg`) y viñeta interior.
- Navegar arrastrando (pointer events, `touch-action: pan-x pan-y`) y con scroll/rueda; limitar el desplazamiento a los bordes.
- Marcadores en el mismo espacio 1400×800 pero con tamaño constante en pantalla (≥44 px táctil): dividir tamaño entre la escala (`--s`).
- Menú flotante ocultable con una ficha (`aria-expanded`). Oculto: solo la ficha y la tarjeta de la ubicación elegida (Unirse / Crear). Recordar el estado en `localStorage`.
- Clic en ubicación → tarjeta flotante; el tema de la partida = nación de la ubicación (`data-nation` en `<body>`).
- Vertical: ventana de mapa + hoja inferior (ya diseñado); horizontal/escritorio: mapa a pantalla completa.

## Partida
- Escena con color de la nación de la ubicación (`.n-*` = `data-nation`); cada jugador con su placa en colores de su nación (`.pw/.pe/.pf/.pa/.pl`).
- Tablero: textura de agua (`assets/textures/water-texture.png`), barcos y marcadores desde `assets/sprite.svg` (`<use href="/assets/sprite.svg#carrier">`). Variables `--acc`/`--acc2` en el `<svg>` del tablero dueño de los barcos.
- Salvos por nación: `hit-{water|earth|fire|air|lotus}`, `miss` (color de nación), `target`, `sunk`. Emblemas `emb-*`.
- Tablero 10×10 en móvil se mantiene por ahora.

## Colocar flota
- Arrastrar y soltar con pointer events (no HTML5 DnD: no funciona en táctil) con casilla magnética y sombra verde/roja si es válido/inválido.
- Girar: ficha ⟳, doble toque, tecla R, rueda o clic derecho. Botones: Al azar, Listo (activo con 5/5).
- Accesible por teclado: seleccionar barco, flechas para mover, R para girar, Enter para fijar.

## Loto Blanco
Nueva nación solo para jugadores (no es ubicación del mapa): añadir `LOTUS` a `NationType` y al selector; tokens (`data-nation="lotus"`) y sprite ya listos.

## PWA
`manifest.webmanifest`, iconos (`public/icons`, origen `icon.png` de legacy; regenerar desde vector si se quiere más nitidez) y `<meta theme-color>` ya enlazados. Falta el service worker (caché de la app y fallback offline): lógica, para la sesión principal (p. ej. `vite-plugin-pwa`). El juego necesita red; offline solo el shell.
