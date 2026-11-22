# 815 · qa · Tests en rojo: perfil, selector de nación del registro, resultado
- Estado: hecha · Origen: "tests nuevos en rojo para los puntos 1-3 de 812" · Riesgo: bajo
- Implementa frontend-dev (tarea 816, junto con el cambio de `GameMap.tsx`).

## Resultado
Rojo (6 casos / 5 archivos; tras esto `npm test`: 5 fallos + ProfileScreen sin módulo):
- `ProfileScreen.test.tsx` (3 casos, falla el import): `.profile[data-nation="air"]`; `.profile-head` con `use[href="/assets/sprite.svg#emb-air"]` y nombre; `dl.profile-stats > div(dt,dd)` = Partidas 6 / Victorias 3 / Derrotas 2.
- `router.test.ts`: `parseRoute("#/profile")` = `{name:"profile"}`.
- `LobbyScreen.design.test.tsx`: enlace "Perfil" `href="#/profile"` en el lobby.
- `LoginScreen.nation.test.tsx`: en registro, `.nation-choice` con 5 botones (aria-label Agua, Tierra, Fuego, Aire, Loto Blanco) con `use href=/assets/sprite.svg#emb-{water,earth,fire,air,lotus}`; `aria-pressed` exclusivo (ninguno inicialmente). En login no existe `.nation-choice` (verde, ya cumple).
- `GameMap.test.tsx` (tarea 813): href `/assets/map.svg`.

Verde añadido (caracterización): `GameScreen.result.test.tsx` LOST y TIED (data-outcome, h2 Perdiste/Empate, img, `.result-actions a`). WON ya existía; el punto 1 ya está implementado.

Contratos inventados:
- Pantalla `ProfileScreen` (default export, sin props, `useAuth().profile`), ruta `#/profile`, enlace desde el menú del lobby.
- `data-nation` en minúsculas (`air`), como los tokens CSS.
- "Partidas" = won + tied + lost (la API no da total); Victorias = `stats.won`; Derrotas = `stats.lost`. `score`, `tied` y `win_rate` existen pero no se exigen.
- El registro NO envía la nación (`register` solo toma name/email/password); los tests solo fijan selector y estado. Persistirla (PATCH /api/players/me tras el login) queda sin test: decidir.
