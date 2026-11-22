# 812 · frontend-dev · Pantallas de resultado, perfil y registro + mapa SVG
- Estado: pendiente · Origen: "haz todo lo que haga falta" (sesión de diseño) · Riesgo: bajo (solo presentación)
- Los estilos ya existen en `frontend/src/styles/screens.css` (importado desde `app.css`). Falta añadir las clases/atributos en los `.tsx`:
  1. **Resultado** (`GameScreen`, fase FINISHED): contenedor `className="result" data-outcome={WON|LOST|TIED}` con imagen, `<h2>` y acciones en `.result-actions`.
  2. **Perfil**: `.profile` (con `data-nation` del jugador), `.profile-head` (emblema `sprite.svg#emb-*` + nombre) y `<dl className="profile-stats">` con `<div><dt/><dd/></div>` (partidas, victorias, derrotas).
  3. **Registro**: selector de nación con `.nation-choice`, botones `aria-pressed` y emblema `emb-*` (incluir `lotus` cuando exista en el backend).
  4. **Mapa**: `GameMap` puede usar `/assets/map.svg` (1400×800, mismas coordenadas que `MAP_LOCATIONS`) en vez de `mapa-game-mobile.png`; el PNG pasa a ser fallback. Los iconos PWA son 96 px ampliados: se regeneran cuando haya vector del icono.
- Hecho cuando: `npm test` y e2e verdes; revisión visual a 390, 844×390 y 1280, claro y oscuro.
