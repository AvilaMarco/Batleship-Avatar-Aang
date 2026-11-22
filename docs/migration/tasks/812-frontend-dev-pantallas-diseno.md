# 812 · frontend-dev · Pantallas de resultado, perfil y registro + mapa SVG
- Estado: hecha · Origen: "haz todo lo que haga falta" (sesión de diseño) · Riesgo: bajo (solo presentación)
- Los estilos ya existen en `frontend/src/styles/screens.css` (importado desde `app.css`). Falta añadir las clases/atributos en los `.tsx`:
  1. **Resultado** (`GameScreen`, fase FINISHED): contenedor `className="result" data-outcome={WON|LOST|TIED}` con imagen, `<h2>` y acciones en `.result-actions`.
  2. **Perfil**: `.profile` (con `data-nation` del jugador), `.profile-head` (emblema `sprite.svg#emb-*` + nombre) y `<dl className="profile-stats">` con `<div><dt/><dd/></div>` (partidas, victorias, derrotas).
  3. **Registro**: selector de nación con `.nation-choice`, botones `aria-pressed` y emblema `emb-*` (incluir `lotus` cuando exista en el backend).
  4. **Mapa**: `GameMap` puede usar `/assets/map.svg` (1400×800, mismas coordenadas que `MAP_LOCATIONS`) en vez de `mapa-game-mobile.png`; el PNG pasa a ser fallback. Los iconos PWA son 96 px ampliados: se regeneran cuando haya vector del icono.
- Hecho cuando: `npm test` y e2e verdes; revisión visual a 390, 844×390 y 1280, claro y oscuro.

## Resultado
- Punto 1 ya estaba; 2: `ProfileScreen`, ruta `#/profile`, enlace "Perfil" en el menú; 3: `.nation-choice` con aria-pressed y emblemas (incl. lotus); la nación se persiste en `register` (`AuthContext`: login, `setNation`, refreshProfile) con test propio; 4: `GameMap` usa `/assets/map.svg`.
- `npm run build` OK; `npm test`: 133/134. El rojo restante es un fallo del test (`getAllByRole` con 0 resultados): tarea 817-qa. Sin revisión visual ni e2e.
