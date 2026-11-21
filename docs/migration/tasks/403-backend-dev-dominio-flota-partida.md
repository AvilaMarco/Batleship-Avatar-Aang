# 403 · backend-dev · Dominio: celdas, flota, reglas de partida, estadísticas y errores
Estado: hecha
Fase: F4
Depende de: 402
Paralelizable: no (la 404 y la 405 empiezan cuando esta termina)

Objetivo: crear las reglas del juego como Java puro con las firmas de ADR 005 Anexo B. **Todavía no se conectan a los servicios** (eso es 406-409), así que no cambia ningún comportamiento observable.

Archivos (`P = src/main/java/dev/racnar/shipbender`):
- nuevos: `P/shared/error/ErrorCode.java`, `P/shared/error/ShipbenderException.java`, `P/game/domain/Cell.java`, `P/game/domain/ShipPlacement.java`, `P/game/domain/Fleet.java`, `P/player/domain/PlayerStats.java`;
- modificados: `P/game/domain/ShipType.java`, `P/game/domain/Game.java`, `P/game/domain/GamePlayer.java`, `P/game/domain/Ship.java`.

Diseño (cerrado):
- `ErrorCode`: exactamente los códigos de ADR 004 Anexo C, cada uno con su status (`int`). `ShipbenderException(ErrorCode code, String detail)`, `code()`, `getMessage()` = `detail`. Conviven con la jerarquía vieja hasta 407.
- `ShipType`: quitar `NONAME` y `@JsonEnumDefaultValue`; agregar `size` (5, 4, 3, 3, 2).
- `Cell.parse`: regex `^([A-J])([1-9]|10)$`, solo mayúsculas; fuera de eso `INVALID_FLEET` con `detail` `"celda inválida: <valor>"`.
- `Fleet` (constructor compacto), en este orden, primer fallo → `INVALID_FLEET`: lista no nula; exactamente un barco por cada `ShipType`; `cells.size() == type.size()`; celdas en línea recta y consecutivas (ordenarlas antes de comparar); sin celdas repetidas entre barcos. `detail` nombra el tipo y la celda (`"SUBMARINE se superpone en A3"`).
- `Game`: `join`, `placeShips`, `participant`, `opponentOf`, `status()` según Anexo B. `join` crea `new GamePlayer(player, this)` y lo agrega a `gamePlayers`. `placeShips` crea un `Ship` por `ShipPlacement` (`type`, `locations` = `cell.toString()`). Orden de guardas de `placeShips`: participante → no tiene barcos. `status()` no usa índices de lista.
- `GamePlayer.ships` se inicializa en `new HashSet<>()` (hoy es `null` hasta `setShips` y `hasShips()` daría NPE en objetos nuevos).
- `PlayerStats.of`: lista vacía → todo en 0 y `winRate` 0.0; `winRate = won * 100.0 / scores.size()`.
- No borrar todavía `Game.updateStatusGpOf/updateStatusGameOf`, `GameValidation` ni las excepciones viejas: los usan los servicios hasta 407.

Criterios de aceptación:
- `grep -rlE "org\.springframework|tools\.jackson|com\.fasterxml" src/main/java/dev/racnar/shipbender/game/domain src/main/java/dev/racnar/shipbender/player/domain src/main/java/dev/racnar/shipbender/shared/error/ErrorCode.java src/main/java/dev/racnar/shipbender/shared/error/ShipbenderException.java` → sin salida.
- `grep -n "NONAME" -r src/main/java` → sin salida.
- Las firmas públicas coinciden con ADR 005 Anexo B (las revisa qa en la 404).
- `./mvnw test` → BUILD SUCCESS, 40 tests, 0 fallos.
- Commit `feat: reglas de flota y partida en el dominio (tarea 403)`.

## Resultado
Hecho: `ErrorCode` (14 códigos, ADR 004 Anexo C), `ShipbenderException`, `Cell`, `ShipPlacement`, `Fleet`, `PlayerStats`; `ShipType` con `size()` y sin `NONAME`; `Game.join/placeShips/participant/opponentOf/status`; `GamePlayer.ships` inicializado; `Ship(ShipType, List<String>)` agregado. Sin conectar a servicios.
Verificación: grep de Spring/Jackson en dominio y errores → sin salida; grep NONAME en `src/main/java` → sin salida; `./mvnw test` x2 → 40 tests, 0 fallos, BUILD SUCCESS.
Notas para qa/404:
- `Game.join` comprueba ALREADY_IN_GAME antes que GAME_FULL (quien ya está en una partida llena recibe ALREADY_IN_GAME).
- `Fleet`: orden de guardas = nulo → tipo nulo/repetido/faltante → tamaño → línea recta → solapes. `detail` de tipo repetido: `"X repetido"`, faltante: `"falta X"`.
- `Cell.parse(null)` → INVALID_FLEET `"celda inválida: null"`.
- `V1__init.sql` aún permite `NONAME` en el check de `ship.type` (inofensivo; limpiar si se toca la migración).
