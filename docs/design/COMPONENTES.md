# Componentes — dirección elegida: **Pergamino** (4 naciones)
Fuente de verdad visual: `docs/design/board/tokens.css` (+ `index.html` como referencia de maquetación).
Ver: `?compare=lobby` (direcciones) · `?nations=game&d=pergamino` (naciones). Servir con `python -m http.server` en la raíz del repo.

## Tokens (migrar a `frontend/src/index.css` como variables en `:root`/`[data-nation]`)
- Nación (`data-nation="water|earth|fire|air"` en `<body>`, según la nación del jugador): `--n-lite --n-mid --n-soft --n-deep`.
- Superficie: `--bg --surface --ink --ink-2 --line --accent(=n-deep) --accent-ink --map-veil`.
- Forma: `--radius 18px` (controles) · `--radius-card 22px` · `--border-w 3px` · `--shadow 3px 3px 0 var(--ink)`.
- Tipo: `--font-title 'Permanent Marker'` (solo h1/h2, auto-hospedar o `display=swap`), cuerpo `system-ui`; inputs 16 px.
- Tablero: `--c-water --c-ship --c-hit --c-miss --c-sel`. Escala de espacio 4/8/12/16/24/32.
- Motivo por nación (aún sin dibujar): agua = olas, tierra = grietas, fuego = llama, aire = espiral; **uno** solo, como divisor bajo `h1`/borde de carta. Acento = `--accent`.
- Movimiento: 150 ms `transform/opacity`; todo bajo `@media (prefers-reduced-motion: no-preference)`.

## Pantalla móvil (contenedor)
`.phone`: ancho `min(480px,100%)` centrado, padding 16, gap 16, fondo `--bg`. Una acción primaria visible por pantalla. Lo frecuente abajo (pulgar).

## Elementos
| Elemento | Tokens | Estados | Táctil | Asset |
|---|---|---|---|---|
| **Botón** `.btn` / `.primary` | surface/ink, border-w, radius, shadow; primario: `--accent` + `--accent-ink` | hover: `translate(-1px,-1px)`; active: sombra 0 y `translate(3px,3px)`; foco: outline 3px `--accent` offset 3; disabled: opacidad .5; loading: spinner dentro, mismo ancho | alto 48 | — |
| **Input** + label arriba | surface, border-w, radius, 16 px | foco: outline accent; error: borde `--c-hit` + mensaje debajo con ⚠ y `aria-describedby` | 48 | — |
| **Alerta** `.alert` | borde `--c-hit`, texto `--ink`, “⚠” | error / éxito (✓ y `--n-mid`) | — | — |
| **Carta de nación** `.nation` | border-w, radius-card, acento | normal (opacidad .7), seleccionada (outline accent + ✓), foco, disabled | ≥ 80×104 (4 por fila a 360 px) | `img/card-{agua,tierra,fuego,viento}.png` |
| **Punto del mapa** `.dot` | surface/ink, accent | **vacía** `+` (opacidad .75) · **unirse** `1` (relleno accent) · **mía** `2` (accent + anillo surface) · **llena** `2` (borde discontinuo, opacidad .45, disabled) · foco | 44×44 (hoy mín. 24: subirlo) | `img/fondo-mapa.png` (→WebP) |
| **Leyenda del mapa** | `--ink-2` 13 px | — | — | texto: “+ crear · 1 unirse · 2 llena” |
| **Ranking** `.rank` | card, línea `--line`, nº tabular | vacío: “Aún no hay partidas ganadas” | filas 44 | `img/nacion*.png` (opcional, 24 px) |
| **Celda** `.cell` | `--c-*`, símbolo ink-2 | `▣` barco · `X` impacto · `•` fallo · `?` desconocida · `◎` seleccionada (outline 2px ink) · foco · disabled (turno rival) | celda ≈ 30 px en 360 px: **dejar tocar un área ≥44** (zoom del tablero o selección por arrastre) | — |
| **Selector de barcos** `.ships` | surface, radius, border-w | normal · elegido (outline accent) · colocado (opacidad .5 + ✓) · scroll horizontal | alto 44 | `ships/*.png` (alto 32) |
| **Botón Disparar** | `.btn.primary` ancho completo | “Disparar (2/3)”; disabled hasta 3 objetivos (mostrar qué falta); loading al enviar | 48 | icono opcional `icons/Cannon.png` 20 px |
| **Barra de emotes** `.emotes` | surface, radio 50% | normal · foco · enfriamiento (opacidad .5) | 44×44 | `emotes/*.png` |
| **Banner de turno** | `h2` + `--ink-2` | “Tu turno” / “Turno rival” / “Esperando rival” | — | — |
| **Pantalla de resultado** `.result` | card, h2 | victoria / derrota (mismo layout) | botón 48 | `img/victory.png`, `img/defeat.png` |
| **Cabecera** `.head` | `--ink`, emblema | — | botón 44 | `img/nacion<Nación>.png` 36 px |

## Reglas de aplicación para `frontend-dev`
1. Crear variables CSS desde `tokens.css`; sustituir hex de `index.css` y estilos inline de `GameMap/Board`.
2. Poner `data-nation` en `<body>` al conocer la nación (por defecto `water`); el selector de nación previsualiza.
3. Mantener símbolos en celdas (el color no es la única señal); validar AA con `--ink` sobre `--surface` en las 4 naciones.
4. Un solo cambio de tokens por iteración; no tocar el marcado para retocar estilo.
5. Pendiente: dibujar los 4 motivos, comprimir `fondo-mapa.png` y `border-*.png`, recuperar texturas del front viejo si se quieren.

## Pantallas añadidas (v2)
- **Resultado** `.result[data-outcome]`: victoria con degradado de la nación, derrota desaturada, empate neutro.
- **Perfil** `.profile` + `.profile-stats`: fondo con la nación del jugador, 3 cifras en fuente de pincel.
- **Registro** `.nation-choice`: botones con emblema; la nación elegida se pinta, las demás en gris.
- **Mapa** `assets/map.svg`: versión plana 1400×800 (mismas coordenadas que `MAP_LOCATIONS`); el PNG queda como fallback.
- **Noche**: tablero, celdas, mapa, resultado y perfil tienen ajuste propio en `styles/screens.css`.
- Pendiente de arte: icono PWA en vector (hoy 96 px ampliado) y licencias de logo/mapa/fondos de Avatar (terceros).
