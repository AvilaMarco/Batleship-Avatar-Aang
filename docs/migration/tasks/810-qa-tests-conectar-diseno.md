# 810 · qa · Tests del front para conectar el diseño (rojo hasta 808)
Estado: hecha
Fase: F8
Depende de: —
Paralelizable: sí (solo front; sin build compartido con 809)
Origen: tarea 808 ("tests primero; solo `qa` toca tests existentes"); `docs/design/NOTAS-IMPLEMENTACION.md`.
Riesgo: bajo

Objetivo: fijar con tests (rojos hasta 808) los puntos 1-5 de 808 que se pueden probar en Vitest/Testing Library: `Board` dibuja barcos y marcadores con `<use href="…sprite.svg#…">` (ids `carrier|battleship|cruiser|submarine|patrol`, `hit-{nación}`, `miss`, `target`, `sunk`) y conserva aria-label/símbolo+texto; `GameScreen` pone `data-nation` en `<body>` y clase `.game` y una placa `.player` por jugador; menú flotante del mapa con `aria-expanded` y estado en localStorage; colocar flota (pointer events, girar con R / doble toque / rueda, válido/inválido, Al azar, Listo); `LoginScreen` sin texto de relleno. Fuera de alcance: service worker (7), LOTUS (809) y la revisión visual.

Criterios: `cd frontend && npm test` → fallan solo los casos nuevos (lista exacta en el Resultado). Commit `test: conectar el diseño al front (tarea 810, rojo hasta 808)` + línea Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado

Archivos nuevos (los existentes no se tocan): `frontend/src/components/Board.test.tsx`, `screens/GameScreen.design.test.tsx`, `screens/LobbyScreen.design.test.tsx`, `screens/LoginScreen.design.test.tsx`.

`npm test`: 14 rojos (solo los nuevos), 102 verdes (los previos intactos).

Casos rojos (hasta 808):
1. Board: impacto, fallo y objetivo se dibujan con `<use>` del sprite y conservan aria-label y texto
2. Board: el impacto usa el marcador de la nación del dueño (prop `nation="FIRE"` -> `hit-fire`)
3. GameScreen: la escena lleva .game y data-nation del body = nación de la ubicación
4. GameScreen: una placa .player por jugador con emblema `emb-*`
5. GameScreen: mi tablero dibuja cada barco con su sprite
6. GameScreen: arrastrar un barco (pointer events) a una celda lo coloca
7. GameScreen: mientras se arrastra, la celda marca `data-drop` valid/invalid
8. GameScreen: tecla R gira el barco seleccionado
9. GameScreen: la rueda sobre el tablero gira el barco
10. GameScreen: doble toque sobre el barco lo gira
11. GameScreen: Al azar coloca los 5 barcos y Listo envía la flota
12. Lobby: menú flotante con ficha (`aria-expanded`, nombre /menú/i) recordado en localStorage
13. Login: sin encabezado; audio con `<track kind="metadata">` y botón Silenciar (`aria-pressed`)
14. Login: miniatura por nación (agua/tierra/fuego/aire) y una sola con `aria-current="true"`

Contratos que 808 debe respetar (o avisar para ajustar el test): prop `nation` en Board; `data-drop` en la celda; botones "Al azar" y "Listo" (Listo sustituye a "Enviar barcos": los tests previos de GameScreen que usan "Enviar barcos" los actualiza qa cuando 808 lo cambie); `data-nation` en minúsculas.
No cubierto: service worker (7), LOTUS (809), arrastre del mapa y revisión visual.
