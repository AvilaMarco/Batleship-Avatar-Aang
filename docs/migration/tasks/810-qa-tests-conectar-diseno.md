# 810 · qa · Tests del front para conectar el diseño (rojo hasta 808)
Estado: pendiente
Fase: F8
Depende de: —
Paralelizable: sí (solo front; sin build compartido con 809)
Origen: tarea 808 ("tests primero; solo `qa` toca tests existentes"); `docs/design/NOTAS-IMPLEMENTACION.md`.
Riesgo: bajo

Objetivo: fijar con tests (rojos hasta 808) los puntos 1-5 de 808 que se pueden probar en Vitest/Testing Library: `Board` dibuja barcos y marcadores con `<use href="…sprite.svg#…">` (ids `carrier|battleship|cruiser|submarine|patrol`, `hit-{nación}`, `miss`, `target`, `sunk`) y conserva aria-label/símbolo+texto; `GameScreen` pone `data-nation` en `<body>` y clase `.game` y una placa `.player` por jugador; menú flotante del mapa con `aria-expanded` y estado en localStorage; colocar flota (pointer events, girar con R / doble toque / rueda, válido/inválido, Al azar, Listo); `LoginScreen` sin texto de relleno. Fuera de alcance: service worker (7), LOTUS (809) y la revisión visual.

Criterios: `cd frontend && npm test` → fallan solo los casos nuevos (lista exacta en el Resultado). Commit `test: conectar el diseño al front (tarea 810, rojo hasta 808)` + línea Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado
