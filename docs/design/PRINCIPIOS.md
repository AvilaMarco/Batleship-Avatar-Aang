# Principios de diseño (base reutilizable; aplicados a Shipbender)
Complementa la skill global `diseno-ui` (no la duplica). Si hay dudas, manda el archivo de tokens.

## Reglas generales
1. **Mobile-first**: se diseña a 360–390 px; escritorio = el mismo móvil centrado (máx. ~480 px) hasta que haga falta más. Acciones frecuentes en la zona del pulgar (mitad inferior).
2. **Táctil ≥ 44×44 px** (el punto del mapa hoy mide ≥24: el área de toque debe crecer aunque el dibujo sea menor).
3. **Minimalismo**: una acción principal por pantalla (login: Entrar · lobby: elegir punto del mapa · partida: Disparar). Jerarquía por tamaño y espacio antes que por color; 3 niveles de tinta.
4. **Tokens en variables CSS** por rol (no por valor): color, tipografía, espacio (escala 4 px), radio, sombra, movimiento. El HTML/React nunca contiene hex ni px sueltos.
5. **Contraste WCAG AA**: 4.5:1 texto, 3:1 controles/iconos. Verificar cada par `--ink` sobre `--surface` por nación.
6. **El color nunca es la única señal**: celdas ▣ barco · X impacto · • agua · ? incógnita; puntos del mapa con número/“+”; estados con texto o símbolo.
7. **Modo oscuro** por variables (`prefers-color-scheme` + `data-theme`); superficies más claras = más elevación.
8. **`prefers-reduced-motion`**: sin animaciones o fade corto. Animar solo `transform`/`opacity`, 150 ms micro, 300 ms superficies.
9. **Estados siempre**: hover, `:focus-visible`, active, disabled, loading, vacío, error.
10. Texto ≥ 16 px en inputs (iOS no hace zoom); `font-variant-numeric: tabular-nums` en contadores.

## Identidad por nación (sin una hoja de estilos por nación)
Nación = **paleta (≈6 tokens) + 1 motivo + 1 acento**. Mismo CSS, mismos componentes; solo cambia `data-nation` en `<body>`.
- Motivo = un patrón/forma recurrente (olas, grietas, llama, espiral…) usado como borde, divisor o fondo muy tenue — nunca en cada elemento.
- Acento = el único color saturado (acción primaria, foco, punto activo).
- La nación se **ve** en: cabecera, botón primario, borde de carta, fondo del mapa/tablero. Todo lo demás es neutro.
- Paleta de partida: la del juego original (ver `INVENTARIO.md` §2), reajustada para AA.

## Uso de los assets existentes
- Cartas `card-*.png` solo en el selector de nación (son la ilustración protagonista). Emblemas `nacion*.png` como marca pequeña (cabecera, ranking).
- `fondo-mapa.png` solo en el lobby, a ancho completo, con los puntos encima (convertirlo a WebP ~<300 KB).
- `victory/defeat.png` solo en la pantalla de resultado. Barcos solo en el selector de barcos, tamaño ≤ 64 px alto.
- Prohibido: ilustración detrás de texto sin velo, 2 assets “héroe” en la misma pantalla, texturas en controles pequeños.
- Pendiente de limpieza (no es diseño): renombrar `selec-AIR`, `señorx`, extensiones erróneas, quitar duplicado `logo.png`.

## Proceso barato en tokens
Brainstorm = editar **solo** `board/tokens.css` (o `data-direction`/`data-nation` en `<body>`); jamás reescribir `index.html`. Una captura 0.5× por dirección. Aprobado → `COMPONENTES.md` → un `frontend-dev` lo aplica a `frontend/src`.

## Reglas de Modern Web Guidance (guías `css`, `dark-mode`, `form-factor-adaptation`, `motion`)
- **CSS**: orden de capas declarado arriba (`@layer reset, base, theme, components;`); tokens en `:root`, sin resets globales agresivos.
- **Oscuro**: `<meta name="color-scheme" content="light dark">` + `color-scheme: light dark` en `:root`; tokens con `light-dark(claro, oscuro)` y, para navegadores antiguos, variables + `@media (prefers-color-scheme: dark)`; `accent-color` de la nación en controles nativos. Pergamino oscuro = “pergamino de noche” (por hacer).
- **Táctil**: `--target-size: 24px` y `44px` con `@media (pointer: coarse)`; nunca menos de 24 (WCAG 2.5.8).
- **Móvil**: contenedores con `@container` para componentes (tablero, carta); `viewport-fit=cover` + `env(safe-area-inset-bottom)` en la barra de Disparar/emotes; `scroll-padding-bottom` si hay barra fija.
- **Movimiento**: animar solo dentro de `@media (prefers-reduced-motion: no-preference)`; nada de `animation-duration: .01ms` global.
