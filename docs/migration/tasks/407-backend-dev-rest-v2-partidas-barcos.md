# 407 · backend-dev · REST v2: partidas y barcos con el dominio conectado
Estado: hecha
Fase: F4
Depende de: 406
Paralelizable: no

Objetivo: implementar ADR 004 Anexo A.2–A.4 usando `Game`/`Fleet` de la 403, fusionar los servicios de partida en `GameService` y quitar el contrato v1 de partidas, hasta que `./mvnw test` quede **todo en verde**.

Archivos (`P = src/main/java/dev/racnar/shipbender`): `P/game/**`, `P/player/SalvoService.java` y `P/player/SalvoController.java` (se borran), `P/player/dto/MenuViewDTO.java` (se borra), `P/shared/error/**`.

Diseño (cerrado):
- `GameController`: `GET /api/games`, `POST /api/games` (201 + `Location`), `POST /api/games/{id}/players` (201), `GET /api/games/{id}`, `POST /api/games/{id}/ships` (201). Recibe `Principal`, convierte el request a `Fleet` (`Cell::parse`) y devuelve records.
- `GameService` (`@Transactional` en los comandos) absorbe `MatchService`, `GameplayService.createShips`, `service/model/GameService`, `GamePlayerService` y `GameValidation`. Cada comando: cargar `Player` por email → cargar `Game` (404 `GAME_NOT_FOUND`) → método del dominio → `save` → devolver `GameView.of(game, playerId)`. "Location ocupada" sigue siendo una consulta del repositorio (`LOCATION_TAKEN`). `join` persiste por cascada desde `Game`; `GamePlayerRepository` se borra si queda sin uso.
- `GameView.of(game, playerId)`: arma `me` y `opponent` desde `participant`/`opponentOf`; **no muta entidades** (se elimina `emptyShips()` y el truco de `GameService.java:39-46`). `opponent` sin `ships`. `GameSummary` con `PlayerSummary` (sin email).
- Borrar: `MatchController`, `MatchService`, `GameplayController` y `GameplayService` (el emote vuelve en la 409 en `chat`; nadie lo testea hoy), `GamePlayerService`, `GameValidation`, `Game.updateStatusGpOf/updateStatusGameOf`, `GamePlayer.emptyShips`, `SalvoService`, `SalvoController`, `MenuViewDTO`, `StatusGameDTO`, `PlayerStatus`, `GameDataDTO`, `GameMatchDTO`, `GameCreatedDTO`, `GameMapDTO`, `GamePlayerDTO`, `GamePlayerDataDTO`, `ShipDTO`, `CreatedShipsDTO`, `GameDTO`, `SalvoDTO`.
- Borrar `AirbenderException` y sus subclases: todo lanza `ShipbenderException`. Sacar su rama de `RestExceptionHandler`.
- `WebSecurityConfig`: ya cubre `/api/**` desde la 406; verificar que no quedan reglas de `/api/match/**` ni `/api/players/nation/**`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 0 fallos, 0 errores.
- `grep -rn "AirbenderException\|/api/match\|emptyShips\|MatchService\|GameplayService\|GameValidation" src/main/java` → sin salida.
- `grep -rn "ModelMapper" src/main/java/dev/racnar/shipbender/game` → sin salida.
- Swagger (`/docs` con `spring-boot:test-run`) lista solo las rutas de ADR 004 Anexo A. Detener el proceso.
- Commit `feat: REST v2 de partidas y barcos con reglas del dominio (tarea 407)`.

## Resultado

- `GameController` + `GameService` (comandos `@Transactional`) + records `GameView`/`GameSummary`/`CreateGameRequest`/`PlaceShipsRequest`. `GameView.of` solo lee entidades (sin `emptyShips`): el test `placeShips_bothPlayers_...` confirma que A conserva sus 5 barcos y el rival nunca expone `ships`.
- Borrados: servicios/controllers v1 de match, gameplay y salvo, DTOs v1, `AirbenderException` y subclases, `PlayerStatus`, `Game.updateStatus*`, `GamePlayer.emptyShips`, v1 de `PlayerService`, DTOs v1 de player, bean y dependencia ModelMapper. `PlayerNotFoundException` -> `ShipbenderException(UNAUTHENTICATED)`.
- Extra: registro concurrente con el mismo email ahora da 409 `EMAIL_IN_USE` (`saveAndFlush` + captura de `DataIntegrityViolationException`).
- `GamePlayerRepository` se conserva: el seed `dev` de `SpringBeans` lo usa.
- Verificacion: `./mvnw test` x3 -> exit 0, 70 tests, 0 fallos. Swagger (`/docs`, puerto 4704): 10 rutas = Anexo A; proceso detenido por PID.
