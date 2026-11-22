# 808 · frontend-dev · Conectar el diseño visual al front

- Estado: pendiente
- Origen: "pasalo como tarea" (usuario, 2026-11-22); diseños en https://claude.ai/artifact/KBrS8Wx75qZX6acFNe863E; detalle funcional en `docs/design/NOTAS-IMPLEMENTACION.md`.
- Riesgo: bajo (presentación + componentes); dividir en varios commits si pasa ~400 líneas.

Ya en `main`: tokens (incl. `lotus`), `public/assets/sprite.svg`, texturas, manifest e iconos, CSS `.game`, `.player`, mapa con pergamino. Falta conectarlo (tests primero; solo `qa` toca tests existentes).

1. **Tablero** (`Board.tsx`): dibujar barcos y marcadores con `sprite.svg` (`carrier|battleship|cruiser|submarine|patrol`, `hit-{nación}`, `miss`, `target`, `sunk`); `--acc/--acc2` según la nación del dueño. Mantener accesibilidad (aria-label, símbolo+texto, no solo color).
2. **GameScreen**: `data-nation` del `<body>` = nación de la ubicación; clase `.game` en la escena; placa `.player` (emblema `emb-*`) por jugador con su nación.
3. **Mapa/Lobby**: mapa a pantalla completa en horizontal/escritorio (cover, arrastre y scroll, marcadores de tamaño constante), menú flotante ocultable con ficha (`aria-expanded`, estado en localStorage), tarjeta de ubicación. Revisar que `.map { height:100dvh }` en horizontal no rompa el lobby.
4. **Colocar flota**: arrastrar y soltar con pointer events, girar (⟳, doble toque, R, rueda), válido/inválido, Al azar, Listo.
5. **Login**: sin texto ni puntos; miniaturas de naciones (escritorio) sincronizadas con la silueta y el audio (VTT).
6. **Loto Blanco**: `LOTUS` en `NationType`, selector y backend.
7. **PWA**: service worker (p. ej. `vite-plugin-pwa`); manifest e iconos ya están.

Hecho cuando: `npm test` y `npm run e2e` verdes, y revisión visual a 390, 844×390 y 1280.
