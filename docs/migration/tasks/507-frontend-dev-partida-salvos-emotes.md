# 507 · frontend-dev · Pantalla de partida (2/2): salvos, resultado, emotes y errores
Estado: pendiente
Fase: F5
Depende de: 506
Paralelizable: no
Origen: PLAN.md F5 "Partida completa jugable"; ADR 004 Anexo F (salvos, implementado en 413) y Anexo B (emotes, `/user/queue/errors`); inventario de `game.html` ("Send Salvos", `EMOTE_CHAT`, consola).
Riesgo: normal

Objetivo: jugar hasta el final: disparar, ver impactos/hundidos de ambos lados, ver el resultado; mandar y recibir emotes.

Archivos: `frontend/src/screens/GameScreen.tsx` + `GameScreen.test.tsx` (agregar casos), `frontend/src/components/EmoteBar.tsx`.

Diseño (cerrado):
- Fases `FIRE` / `WAITING_OPPONENT_SALVO` / `FINISHED`: `Board` propio (`myBoard`) y `Board` rival (`enemyBoard`). En `FIRE` los clics del tablero rival usan `toggleTarget` (ya disparadas = celdas de `me.salvos`); "Disparar (n/5)" habilitado con 5 → `POST /api/games/{id}/salvos` → `view` de la respuesta. En `WAITING_OPPONENT_SALVO` tablero rival deshabilitado y mensaje. Mostrar `turn` y los `sunk` de cada lado.
- `FINISHED`: `outcome(view)` → "Ganaste" / "Perdiste" / "Empate" con `assets/img/victory.png` o `defeat.png` y link al lobby.
- `EmoteBar`: botones con `assets/emotes/emote-1..4.png` → `sendEmote("emote-N")`; evento `EMOTE` del rival → burbuja 3 s junto a su nombre, solo si `emote` está en la lista conocida (`EMOTES` const en `EmoteBar.tsx`; el backend acepta cualquier string ≤32, no armar una URL con texto del rival) (los propios también llegan por el topic: ignorar los de `me.player.id` o mostrarlos del lado propio).
- `/user/queue/errors` y `ApiError` de salvos (`GAME_NOT_IN_PROGRESS`, `SALVO_ALREADY_FIRED`, `INVALID_SALVO`) → aviso visible con `detail`.

Criterios de aceptación (tests primero; api y stomp mockeados):
- Test: en `FIRE`, elegir 5 celdas habilita "Disparar" y llama `fireSalvo` con esas 5; la 6.ª no se agrega; `view` `FINISH` con `opponent.sunk` de 5 muestra "Ganaste"; evento `EMOTE` del rival muestra la imagen del emote.
- `cd frontend && npm test && npm run build` → exit 0.
- Manual en 4705 (backend 4704), dos pestañas: partida completa hasta "Ganaste"/"Perdiste", con un emote en cada sentido. Si no hay Docker: `Estado: parcial`.
- Commit `feat(front): salvos, fin de partida y emotes (tarea 507)`.

## Resultado
