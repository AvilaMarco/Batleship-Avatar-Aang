# Rediseño desde el original (v1)
Base: el front viejo restaurado (`git show b40e276^:src/main/resources/static/...`), capturado en móvil con API falsa.
Capturas: `capturas/original/01..09`. Objetivo: **atmósfera del mundo de Avatar**, estilos viejos modernizados, quitar lo que no encaja.

## 1. Qué había (capturas)
| # | Pantalla | Qué funciona | Qué falla |
|---|---|---|---|
| 01-02 | Login: silueta por nación (`img/mobile1-4.png`) que rota cada 5 s + audio `main.ogg` + botones Login/Sign in | atmósfera inmediata, nación como protagonista | carrusel con temporizador, **no sincronizado** con el audio; botones negros con borde neón |
| 03 | Menú en vertical | — | roto: el menú era solo horizontal |
| 04 | Menú horizontal: marco `fondo-mapa.png` (título arriba, botones en esquinas, emblema a los lados) + mapa `mapa-game-mobile.png` 1400×800 a medio ver en el centro, con scroll | el “cuadro” con el mapa vivo dentro; navegar el mapa | botones pequeños (<44 px), texto en inglés, marco se come la pantalla |
| 05 | Stats: modal con `bg-{NACIÓN}.png` (vector plano, personaje a la izquierda, espacio vacío a la derecha) + tabla | **el mejor estilo**: plano, claro, minimalista | el fondo de la nación es aleatorio; tabla en negro; “undefined%” |
| 06 | Ranking: tabla negra sobre pergamino | pergamino | contraste/densidad pobres |
| 07 | Partida seleccionada en el mapa: el punto brilla + botones Info/Enter abajo | flujo “toco el sitio → actúo” | estados solo por color |
| 08 | Elegir nación: 4 cartas `card-*.png` | cartas claras y reconocibles | aparece tras login sin contexto |
| 09 | Partida | — | **nunca tuvo diseño** (colores de depuración) |

## 2. Dirección visual: “Pai Sho” (evoluciona Pergamino)
- **Ilustración = vector plano** (como `bg-*`, `card-*`, `nacion*`): bloques de color sin degradados, siluetas, sin contorno. Todo asset nuevo (barcos, iconos, marcadores) se hace en ese estilo, en SVG.
- **Soporte = madera y papel del Pai Sho**: tablero de madera cálida, papel crema, fichas redondas con glifo. Marcadores del mapa, botones circulares y fichas de turno = **fichas de Pai Sho** (círculo con borde, glifo de la nación).
- **Color**: neutros de papel/madera + color de nación tomado de `bg-*`: AGUA `#5a7abd` · TIERRA `#438a17` · FUEGO `#6f1e15` · AIRE `#72c3e5` (reemplazan `--n-*`; ajustar a AA).
- **Tipografía**: títulos con pincel/marcador (Permanent Marker sirve de transición; buscar una brush con licencia abierta); cuerpo `system-ui`.
- Fuera: neones, botones negros, Font Awesome, todas las texturas salvo `textura-mapa.jpg` (opcional) y los `fondoGrid-*`, `bg1/bg4/fondo-99`, `ocean.gif` (1.7 MB).

## 3. Sistema de mapa (rehacer con tecnología actual)
**Hallazgo**: el front nuevo (`GameMap.tsx`, W=1395 H=950) pinta los puntos sobre **`fondo-mapa.png`, que es el marco**, pero las coordenadas (`map-locations.ts`) son del mapa **`mapa-game-mobile.png` 1400×800**. Los puntos salen desplazados y falta el mapa real. Mi prototipo `board/` cometió el mismo error.
- **Un solo SVG** con `viewBox="0 0 1400 800"`: `<image href="mapa">` + un `<a>`/`<g role="button" tabindex="0">` por ubicación, con `<circle>` (o `<path>` con la forma real de la isla) en las **mismas coordenadas del dibujo**. El navegador escala todo junto: clic exacto a cualquier tamaño, sin `resizeCoord` ni `<map>/<area>`.
- **Área táctil**: círculo invisible de ≥ 22 unidades de radio *en pantalla* (`vector-effect` / radio en CSS según zoom); el dibujo puede ser menor.
- **Navegar**: contenedor `overflow: auto` + `touch-action: pan-x pan-y` (scroll nativo, inercia gratis); zoom con pellizco/botones cambiando el `viewBox`. Al entrar, centrar en la región de la nación del jugador (`scrollIntoView` sobre su capital).
- **Datos**: `map-locations.json` con `id, nation, name (Ba Sing Se, Omashu…), x, y, r` en coords 1400×800; los nombres ya están dibujados en el mapa.
- **Calidad**: el mapa es un JPG fan-made de baja resolución con artefactos. Opción A: reescalado ×2 + WebP. Opción B (recomendada): **redibujarlo en SVG plano** (olas de Pai Sho como patrón, islas planas por nación); las coordenadas se conservan si se mantiene el lienzo 1400×800.
- **Crear partida tocando el mapa**: ubicación vacía → hoja inferior “Crear partida en Omashu” (1 acción); con 1 jugador → “Unirse”; mía → “Volver”; llena → “Mirar/llena”. Estado = ficha + número/glifo, no solo color.
- **Tema por ubicación**: la partida toma `data-nation` **de la ubicación**, no del jugador (partida en Sun Sei → tablero y fondo de Fuego). El perfil del jugador sigue personalizando stats/emblema.
- **Marco** (`fondo-mapa.png`): se conserva la idea (título arriba, mapa dentro) pero como marco fino dibujado en CSS/SVG; las acciones van a una barra inferior al alcance del pulgar.

## 4. Login con audio sincronizado
- `main.ogg` dura 20 s, silencio hasta 4.4 s. Golpes de volumen estimados (sin oírlo): **4.3 s, 7.7 s, 9.5 s, 12.5 s** → agua, tierra, fuego, aire. **Confirmar de oído**.
- Implementación: `<audio>` + `<track kind="metadata" src="intro.vtt">`; cada cue trae la nación; en `cuechange` se cambia `data-nation` y la imagen (`mobile1-4`, rehechas en vector plano). Los tiempos viven en el `.vtt`: se ajustan sin tocar código.
- El navegador bloquea el autoplay con sonido: la secuencia arranca con un toque (“Entrar al mundo”); botón de silencio visible; con `prefers-reduced-motion` → fundido o imagen fija.
- Formulario de login sobre la imagen en una hoja inferior (inputs 48 px, 16 px de texto).

## 5. Nación del perfil, stats y barcos
- **Elegir nación** pasa al **registro** (paso 2 de “Crear cuenta”, con `card-*`). Se puede cambiar en el perfil.
- **Stats**: `bg-{NACIÓN}` del jugador (no aleatorio), personaje a la izquierda y números a la derecha (en vertical: personaje arriba recortado con `object-position`). Ganadas/empatadas/perdidas + % sin “undefined”. Bajar los PNG de 3840×2160 a WebP ~1600 px.
- **Barcos**: rehacer en SVG vector plano, vista cenital, largo = N celdas exactas. Por nación: Tribu Agua (canoas/barcos de hueso de ballena), Reino Tierra (barcazas de piedra), Nación del Fuego (acorazados de hierro con chimeneas), Aire (planeadores/bisonte). El juego usa el set de la nación de la partida.
- **Partida**: diseño nuevo (nunca hubo): tablero como cuadrícula de Pai Sho sobre madera; celdas con fichas (▣ X • ? se mantienen como glifo).

## 6. Assets a recuperar a `frontend/public/assets/`
`web/mapa-game-mobile.png` (mapa real), `web/assets/wallpapers/bg-{WATER,EARTH,FIRE,AIR}.png`, `img/mobile1-4.png`, `main.ogg`, `img/audio.png`/`no-audio.png` (o SVG), opcional `texturas/textura-mapa.jpg`.
Ojo licencias: el logo de Avatar, el mapa fan-made y los `bg-*` (firma de su autor) son de terceros; vale para un proyecto personal, no para publicarlo como propio.

## 7. Decisiones tomadas (Marco, 2026-11-22)
1. **Orientación**: login en **vertical**; menú/mapa y partida pensados para **horizontal**, con adaptación a vertical (`@media (orientation: portrait)`: mapa a pantalla completa con scroll, tableros apilados).
2. **Mapa**: **redibujar en SVG plano** sobre el lienzo 1400×800 (coordenadas intactas).
3. **Pai Sho**: fichas y marcadores **y** el tablero de la partida sobre madera estilo Pai Sho.
