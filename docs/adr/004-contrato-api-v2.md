# ADR 004 — Contrato API v2 (REST + STOMP)

Estado: propuesta · 2026-11-21 · Se congela al aprobarla el usuario; desde ahí F5 puede empezar en paralelo.

## Contexto
El contrato v1 mezcla datos en la ruta (`/api/match/games/{nation}/{location}`), expone `GamePlayer`, responde `Map`/DTOs genéricos, coloca barcos por un `/topic` compartido (el rival recibe tus posiciones, confirmado en F3) y pierde los errores del WebSocket. Lineamientos: ADR 002 punto 4.

## Decisión
1. **Comandos que cambian estado persistente van por REST** (crear, unirse, colocar barcos; en v2.1 disparar). **STOMP solo para eventos del servidor y mensajes efímeros** (emotes; en v2.1 chat). Una sola vía por acción.
2. Recursos: `/api/auth`, `/api/players`, `/api/games`. `GamePlayer` no se expone: la vista de una partida es relativa a quien pregunta (`me` / `opponent`).
3. **Un topic público por partida**, `/topic/games/{id}`, con eventos tipados por `type` y **sin posiciones de barcos**. El estado privado (tus barcos) solo sale por `GET /api/games/{id}` a su dueño. `/user/queue/errors` lleva los errores de STOMP.
4. **Errores RFC 9457** (`ProblemDetail` de Spring) en REST y en `/user/queue/errors`, con `code` estable (Anexo C).
5. JSON en snake_case (ya configurado en Jackson), DTOs `record`, enums en MAYÚSCULAS. Auth: `Authorization: Bearer <jwt>` en HTTP y en el CONNECT de STOMP.
6. Grilla 10×10 (filas A–J, columnas 1–10), flota clásica de 5 barcos (Anexo A.4). Ver decisión abierta D1.
7. Salvos, fin de partida y chat de texto **no existen hoy**: quedan diseñados como v2.1 (Anexo F), sin implementar salvo decisión del usuario.

## Decisiones abiertas (las resuelve el usuario al aprobar)
- **D1 · Grilla y flota.** Propuesta: 10×10 y 5-4-3-3-2 (el seed comentado usa `J1..J5` y `J10`, `SpringBeans.java:67,85`; los tests usan un CARRIER de 5). Alternativa: 8×8 y 4-4-3-3-2, que es lo que dibuja el front viejo (`grid.js` itera `1..GRID_SIZE-1` con `GRID_SIZE = 9`; `ships.js:132` comentado). Cambiarlo es tocar `Cell` y `ShipType`.
- **D2 · ¿Salvos/turnos/fin de partida en F4?** Hoy no existen (Anexo F). Costo: 3 tareas más (411-413). Beneficio: el criterio de cierre de F5 ("partida completa jugable") los necesita; si no van en F4, hay que hacerlos durante F5. Recomendación: sí, con 5 disparos fijos por turno.
- **D3 · ¿Chat de texto en F4?** Hoy solo hay emotes. Costo: ~1 caso de test y ~3 clases chicas, dentro de 408/409. Beneficio bajo para F5. Recomendación: dejarlo diseñado (Anexo F) y hacerlo después.

## Alternativas descartadas
- Todo por STOMP (como v1): los errores hay que correlacionarlos a mano con la petición y no hay códigos HTTP ni `@Valid` gratis.
- Un topic por tipo de evento (`/ships`, `/emotes`, `/waiting-player`): más suscripciones y sin orden entre eventos de la misma partida.
- Formato de error propio (`ErrorDTO`): RFC 9457 es estándar y Spring ya lo trae.

## Consecuencias
- El front viejo deja de funcionar (ya aceptado en ADR 002). Los tests de caracterización v1 se reescriben (tareas 405 y 408).
- Los eventos no se reenvían: al (re)conectar, el cliente pide `GET /api/games/{id}` y luego se suscribe.
- Cualquier PLAYER puede suscribirse a `/topic/games/{id}`; por eso ahí no viaja nada privado.

---

## Anexo A · REST (estilo OpenAPI)

Todas las rutas salvo login y registro exigen JWT con rol `PLAYER`. Sin token o con token inválido: `401 UNAUTHENTICATED`.

### A.1 Auth y jugadores
| Método y ruta | Request | Respuesta OK | Errores |
|---|---|---|---|
| `POST /api/auth/login` | `{email, password}` | `200 {email, token}` (`token` = `"Bearer eyJ..."`, igual que v1) | 400 `VALIDATION_FAILED`, 401 `INVALID_CREDENTIALS` |
| `POST /api/players` | `{name, email, password}` (`email` ≤30, válido; todos obligatorios) | `201 PlayerProfile` | 400 `VALIDATION_FAILED`, 409 `EMAIL_IN_USE` |
| `GET /api/players/me` | — | `200 PlayerProfile` | 401 |
| `PATCH /api/players/me` | `{nation}` | `200 PlayerProfile` | 400 `MALFORMED_REQUEST` (nación inexistente), 400 `VALIDATION_FAILED` (null) |
| `GET /api/players/ranking` | — | `200 [RankingEntry]`, una entrada por jugador, orden `score` desc | 401 |

Logout: se elimina del servidor. Con JWT sin estado el `LogoutFilter` no hacía nada; el cliente descarta el token.

### A.2 Partidas
| Método y ruta | Request | Respuesta OK | Errores |
|---|---|---|---|
| `GET /api/games` | — | `200 [GameSummary]` partidas no terminadas | 401 |
| `POST /api/games` | `{nation, location}` (`location` no vacío, ≤32) | `201 GameView` + `Location: /api/games/{id}` | 400, 409 `LOCATION_TAKEN` |
| `POST /api/games/{id}/players` | — (se une quien llama) | `201 GameView` | 404 `GAME_NOT_FOUND`, 409 `ALREADY_IN_GAME`, 409 `GAME_FULL` |
| `GET /api/games/{id}` | — | `200 GameView` | 403 `NOT_A_PARTICIPANT`, 404 |
| `POST /api/games/{id}/ships` | `{ships: [ShipPlacement]}` | `201 GameView` | 400 `VALIDATION_FAILED`, 400 `INVALID_FLEET`, 403, 404, 409 `SHIPS_ALREADY_PLACED` |

Orden de las validaciones en `ships`: existe la partida (404) → participa (403) → no colocó antes (409) → flota válida (400).

### A.3 Esquemas
```jsonc
// PlayerProfile (solo para uno mismo: lleva email)
{"id": 3, "name": "Ana", "email": "ana@x.com", "nation": "WATER" /* o null */,
 "stats": {"score": 4, "won": 1, "tied": 1, "lost": 0, "win_rate": 50.0}}
// PlayerSummary (lo que ven los demás: sin email)
{"id": 3, "name": "Ana", "nation": "WATER"}
// RankingEntry
{"player": PlayerSummary, "stats": {...igual que arriba}}
// GameSummary
{"id": 7, "nation": "FIRE", "location": "F1", "status": "CREATED", "players": [PlayerSummary]}
// GameView (relativa a quien pregunta)
{"id": 7, "nation": "FIRE", "location": "F1", "status": "CREATED", "turn": 1,
 "me":       {"player": PlayerSummary, "ships_placed": true, "ships": [ShipPlacement]},
 "opponent": {"player": PlayerSummary, "ships_placed": false}   // null si aún no hay rival; NUNCA lleva ships
}
// ShipPlacement
{"type": "CARRIER", "cells": ["A1", "A2", "A3", "A4", "A5"]}
```
`status`: `CREATED` (falta rival o falta que alguien coloque barcos) → `IN_GAME` (ambos colocaron) → `FINISH` (`finish_date` no nulo; en v2 solo lo pone el seed, en v2.1 el fin de partida). Reemplaza los `host`/`client` `WAITING|WITHOUT_SHIPS|WITH_SHIPS` de v1 por `opponent == null` y `ships_placed`.

### A.4 Reglas de la flota (`INVALID_FLEET`, `detail` dice cuál falló)
Exactamente un barco de cada tipo: `CARRIER` 5, `BATTLESHIP` 4, `SUBMARINE` 3, `DESTROYER` 3, `PATROL_BOAT` 2. Cada celda `^[A-J]([1-9]|10)$` (mayúsculas). Celdas de un barco en línea recta y consecutivas (misma fila con columnas seguidas, o misma columna con filas seguidas; en cualquier orden). Sin solapes entre barcos. Tipo desconocido → 400 `MALFORMED_REQUEST` (se elimina `NONAME`).

## Anexo B · STOMP (estilo AsyncAPI)

- Endpoint: `ws(s)://<host>/the-last-shipbender`, WebSocket nativo, heartbeat `10000,10000`.
- CONNECT con header `Authorization: Bearer <jwt>`. Sin token o inválido: el servidor cierra la conexión (como hoy).
- Prefijos: aplicación `/app`, broker `/topic` y `/queue`, usuario `/user`.

| Canal | Dirección | Quién | Payload |
|---|---|---|---|
| `SEND /app/games/{id}/emotes` | cliente → servidor | participante de la partida | `{"emote": "emote-1"}` (`content-type: application/json`; no vacío, ≤32) |
| `SUBSCRIBE /topic/games/{id}` | servidor → todos los suscriptos | cualquier PLAYER | `GameEvent` (abajo) |
| `SUBSCRIBE /user/queue/errors` | servidor → solo la sesión que falló | quien envió el mensaje | `ProblemDetail` con `instance` = destino del SEND |

Cualquier otro destino de SEND o SUBSCRIBE se rechaza.

```jsonc
// GameEvent en /topic/games/{id}; nunca contiene celdas de barcos
{"type": "PLAYER_JOINED", "game_id": 7, "player": PlayerSummary}
{"type": "SHIPS_PLACED",  "game_id": 7, "player_id": 4}
{"type": "GAME_STARTED",  "game_id": 7, "turn": 1}          // justo después del segundo SHIPS_PLACED
{"type": "EMOTE",         "game_id": 7, "player_id": 4, "emote": "emote-1"}
```
Los eventos se publican después del commit de la transacción: si el cliente reacciona con un `GET`, ve el estado nuevo. Los emotes no se persisten (v1 guardaba el último en `game_player.emote` sin que nadie lo leyera).

## Anexo C · Errores (REST y `/user/queue/errors`)
```jsonc
// Content-Type: application/problem+json
{"type": "about:blank", "title": "Conflict", "status": 409, "code": "GAME_FULL",
 "detail": "La partida 7 está completa", "instance": "/api/games/7/players",
 "errors": {"email": ["must be a well-formed email address"]}}   // errors solo en VALIDATION_FAILED
```
| `code` | HTTP | Cuándo |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Bean Validation (`errors` por campo) |
| `MALFORMED_REQUEST` | 400 | JSON ilegible, enum desconocido |
| `INVALID_FLEET` | 400 | Anexo A.4 |
| `UNAUTHENTICATED` | 401 | Sin token, token vencido o firma inválida (v1 lanzaba una excepción sin responder) |
| `INVALID_CREDENTIALS` | 401 | Login fallido (v1: 404) |
| `FORBIDDEN` | 403 | Rol insuficiente o ruta no listada (deny-by-default) |
| `NOT_A_PARTICIPANT` | 403 | La partida existe pero no estás en ella (v1: 409 "Player Already in Game") |
| `GAME_NOT_FOUND` | 404 | |
| `EMAIL_IN_USE`, `LOCATION_TAKEN`, `GAME_FULL`, `ALREADY_IN_GAME`, `SHIPS_ALREADY_PLACED` | 409 | |
| `INTERNAL_ERROR` | 500 | No esperado; `detail` genérico, sin stack trace |

## Anexo D · Ruta vieja → ruta nueva
| v1 | v2 |
|---|---|
| `POST /api/players/login` | `POST /api/auth/login` |
| `POST /api/players` | `POST /api/players` (body sin cambios) |
| `POST /api/players/logout` | se elimina (cliente descarta el token) |
| `POST /api/players/nation/{nation}` | `PATCH /api/players/me` `{nation}` |
| `GET /api/games` (`MenuViewDTO`: jugador + partidas + ranking) | `GET /api/players/me` + `GET /api/games` + `GET /api/players/ranking` |
| `POST /api/match/games/{nation}/{location}` | `POST /api/games` `{nation, location}` |
| `POST /api/match/games/{id}` | `POST /api/games/{id}/players` |
| `GET /api/match/{id}` y `GET /api/match/{id}/status` | `GET /api/games/{id}` (una sola vista) |
| STOMP `SEND /app/{id}/ships` + `/topic/gameplay/{id}/ships` | `POST /api/games/{id}/ships` + eventos `SHIPS_PLACED`/`GAME_STARTED` |
| STOMP `SEND /app/{id}/emotes` (string crudo) + `/topic/gameplay/{id}/emotes` | `SEND /app/games/{id}/emotes` `{emote}` + evento `EMOTE` |
| `/topic/gameplay/{id}/waiting-player` (el front se suscribía; nadie publicaba) | evento `PLAYER_JOINED` |
| — (errores de STOMP perdidos) | `/user/queue/errors` |

## Anexo E · Hallazgos resueltos por el contrato
| Hallazgo (v1) | Resolución v2 |
|---|---|
| Fuga de barcos: `@SendTo("/topic/gameplay/{id}/ships")` con el `GameDataDTO` de quien colocó (`GameplayController.java:31-35`) | Barcos por REST; al topic solo va `SHIPS_PLACED {player_id}`; `opponent` nunca lleva `ships` |
| "Player Already in Game" cuando NO estás (`GameValidation.java:53-54`) | 403 `NOT_A_PARTICIPANT` |
| Unirse a la propia partida (`MatchService.java:58-60`, validación comentada) | 409 `ALREADY_IN_GAME` (regla en `Game.join`) |
| Barcos inválidos aceptados (`GameplayService.java:44-47`, `ShipType.NONAME`) | 400 `INVALID_FLEET` (regla en `Fleet`) |
| Segundo envío de barcos: `throw new RuntimeException()` que nadie ve (`GameplayService.java:43`) | 409 `SHIPS_ALREADY_PLACED` |
| JWT con firma manipulada: excepción sin respuesta (`JWTAuthorizationFilter.java:39`) | 401 `UNAUTHENTICATED` |
| Login fallido = 404 (`LoginException extends NotFoundException`) | 401 `INVALID_CREDENTIALS` |
| Nación inválida = 409 "Conflict Name Exception" (`ErrorHandlerController.java:34`) | 400 `MALFORMED_REQUEST` |
| Email de todos los jugadores en la lista de partidas y en el ranking (`GamePlayerDTO` → `PlayerDTO.email`) | `PlayerSummary` sin email; email solo en `/me` |
| El cliente podía mandar `salvosImpacted` en sus barcos (`ShipDTO.java:12`, copiado por ModelMapper) | `ShipPlacement` solo tiene `type` y `cells` |

## Anexo F · v2.1 (diseñado, NO implementado; ver decisiones abiertas D2/D3)
Hoy no hay mecánica de disparos: `Salvo.goodShoot`/`shipsDead` (`Salvo.java:32-87`) no los llama nadie, no hay endpoint ni handler, `Game.turn` nunca cambia y `score` solo lo pone el seed. Diseño propuesto (turnos simultáneos, como insinúa el código comentado de empate en `GamePlayer.java:108`):
- `POST /api/games/{id}/salvos` `{cells: [5 celdas]}` → `201 GameView`. Errores: 409 `GAME_NOT_IN_PROGRESS` (status ≠ `IN_GAME`), 409 `SALVO_ALREADY_FIRED` (ya disparaste en este turno), 400 `INVALID_SALVO` (cantidad ≠ 5, celdas repetidas o ya disparadas, fuera de grilla).
- Eventos: `SALVO_FIRED {player_id, turn}` (sin celdas); cuando ambos dispararon el turno N: `TURN_RESOLVED {turn, results: [{player_id, hits: [cells], misses: [cells], sunk: [ShipType]}]}`; luego `turn` = N+1 o `GAME_FINISHED {winner_id | null, scores: {"<player_id>": 3|1|0}}` (gana 3, empata 1, pierde 0, como `PlayerService.java:100-102`). Empate: ambas flotas hundidas en el mismo turno.
- `GameView.me` y `.opponent` agregan `salvos: [{turn, cells, hits}]` y `sunk: [ShipType]`; `opponent` sigue sin barcos.
- Chat: `SEND /app/games/{id}/chat {text}` (no vacío, ≤200) → evento `CHAT_MESSAGE {player_id, text, sent_at}`. No se persiste.
