# Inventario visual (estado: rama migration/f7, HEAD c69bc77)

## 1. Assets vigentes — `frontend/public/assets/` (48 archivos, ~8 MB)
**Mapa**
- `img/fondo-mapa.png` 1395×950 RGBA, **5.3 MB** (el más pesado → pasar a WebP/JPG). Usado en `GameMap.tsx`.
- Puntos del mapa: `src/domain/map-locations.ts` (coords en espacio 1395×950, r=30; 2 agua, 9+ tierra…).

**Por nación** (WATER/EARTH/FIRE/AIR; ojo: los archivos usan agua/tierra/fuego/viento)
- Cartas menú: `img/card-{agua,tierra,fuego,viento}.png` 500×650, 4–48 KB. Usadas en `NationPicker.tsx`.
- Emblema: `img/nacion{Agua,Tierra,Fuego,Viento}.png` 126×171, ~11 KB.
- Iconos: `icons/border-{NATION}.png` 692×161 (446 KB c/u, evitable), `icons/transparent-{NATION}.png` ~176×170, `icons/select-{NATION}.png` 143² (typo: `selec-AIR.png`).

**Partida / menú**
- Resultado: `img/victory.png` 572×291, `img/defeat.png` 576×302 (`GameScreen.tsx`).
- Barcos: `ships/{battleship,carrier,destroyer,patrol_boat,submarine}.png` ~435×256 (`ShipPicker.tsx`).
- Emotes: `emotes/*.png` (6, 140 KB total; `EmoteBar.tsx`).
- Logo: `img/logo.png` (= `icons/logo.png`, duplicado), `img/header.jpg`.
- Iconos varios: `icons/{Cannon,chat,close,emote,explosion2,icon,interrogante,misterx,reload,señorx}.png`.
  Problemas: `chat.png` es JPEG, `hand-grab-icon.jpg` es PNG, `señorx.png` lleva ñ.

**Perdidos en la migración**: `texturas/` (btn-map.png, textura-mapa.jpg, fondoGrid-*) y `wallpapers/bg-*.png`
siguen en git: `git show b40e276^:src/main/resources/static/web/assets/{texturas,wallpapers}/...`.

## 2. Estilos viejos — `git show b40e276^:src/main/resources/static/web/...`
- Archivos: `css/{colores,style,games,game,layout,desktop}.css`, `css/mobile/layout.css`, `sass/utils/_colors.scss`, `sass/games-scss/_buttons.scss`, `games.html`, `game.html`.
- **Paleta por nación** (`_colors.scss`): AIRE `#f9d672`/`#e7da00`/sec `#f3994b`; FUEGO `#d56728`/`#af1b2b`/`#7f2e13`; AGUA `#82d0e6`/`#69b0ce`/`#1e3360`; TIERRA `#6c6b2b`/`#34a646`/`#2c2b1d`.
- Pergamino `#e1d2a7` de fondo; neones `#ecef24 #ec2511 #3965bd #30c530` (descartables).
- Tipografía: **Permanent Marker** (Google Fonts) para títulos; Font Awesome 6 para iconos.
- Botón `.main-btn`: textura, borde 4px, radio 1.5rem; `.main-btn-circle` circular. Carta `.nation-card` radio 25px. Modal overlay z 2000.
- Menú: `games.html` → `.container-map` pantalla completa con textura de mapa, botones arriba/medio, selector de nación abajo. Móvil: breakpoints 576/768, layout landscape.
- **Rescatar**: paleta por nación, Permanent Marker (solo títulos), idea de pergamino y botón redondeado de borde grueso. **Descartar**: neones, `colores.css` legacy, Font Awesome entero (10k líneas).

## 3. Estilos actuales (mínimos)
- `frontend/src/index.css` (11 líneas): sin variables, hex fijos. Celdas `.cell` + `.ship .hit .miss .pending .selected`, `.emote`, `:focus-visible`.
- `Board.tsx`: clases `board`, `cell <estado>` + inline. `GameMap.tsx`: todo inline (botón circular por punto, minWidth 24 ⚠ <44px). `NationPicker.tsx`, `Ranking.tsx`, `ShipPicker.tsx`, `screens/*`: sin estilo.
- Tokens existentes: ninguno.
