# 402 · backend-dev · Reorganizar por feature y borrar las interfaces `I*Service`
Estado: hecha
Fase: F4
Depende de: 401
Paralelizable: no (mueve casi todos los archivos)

Objetivo: llevar las clases existentes a la estructura de ADR 005 Anexo A **sin cambiar comportamiento**, y borrar las 8 interfaces de una sola implementación. La fusión de servicios y los DTOs nuevos llegan en 406-409; acá solo se mueve.

Archivos: todo `src/main/java/dev/racnar/shipbender/**`; en `src/test` solo imports y los tipos de `@Mock` de `unit/service/MatchServiceTest.java`.

Diseño (cerrado). Prefijo `P = src/main/java/dev/racnar/shipbender`. Usar `git mv`.
| Origen | Destino |
|---|---|
| `config/WebSecurityConfig`, `config/websocket/WebSocketSecurityConfig`, `config/ENV_VARIABLES` | `security/` |
| `config/websocket/WebSocketConfig` | `config/` (y borrar `config/websocket/`) |
| `config/SpringBeans` | sigue en `config/` (la 410 lo parte) |
| `enums/NationType` | `shared/` |
| `exception/**` (con subpaquetes) | `shared/error/**` (la jerarquía vieja se borra en 407) |
| `models/Player`, `enums/Rol` | `player/domain/` |
| `controller/PlayerController`, `service/SessionService`, `service/SalvoService`, `service/model/PlayerService`, `repository/PlayerRepository`, `controller/SalvoController` | `player/` (temporal: `SalvoService`/`SalvoController` dependen de `game`; se eliminan en 406-407) |
| `dto/PlayerDTO`, `PlayerScoreDTO`, `PlayerStatsDTO`, `ScoreStatsDTO`, `dto/request/LoginPlayerDTO`, `SignInPlayerDTO`, `dto/response/UserTokenDTO`, `MenuViewDTO` | `player/dto/` |
| `models/Game`, `GamePlayer`, `Ship`, `Salvo`, `enums/GameStatus`, `PlayerStatus`, `ShipType` | `game/domain/` |
| `controller/MatchController`, `GameplayController`, `service/MatchService`, `GameplayService`, `service/model/GameService`, `GamePlayerService`, `service/validations/GameValidation`, `repository/GameRepository`, `GamePlayerRepository`, `ShipRepository`, `SalvoRepository` | `game/` |
| resto de `dto/**` (`GameDTO`, `GameMapDTO`, `GamePlayerDTO`, `GamePlayerDataDTO`, `SalvoDTO`, `request/websocket/*`, `response/*` no listados arriba) | `game/dto/` |
- Borrar `service/intereface/*` (7) y `service/validations/IGameValidation`: quien inyectaba `IXService` inyecta `XService`; quitar `implements` y los `@Override` que sobren. Nada más cambia dentro de los métodos.
- Clases que pasan a otro paquete y tenían visibilidad de paquete (p. ej. `WebSecurityConfig` es `class` sin `public`): hacerlas `public` solo si no compila.
- `MatchServiceTest`: `@Mock IPlayerService` → `@Mock PlayerService`, etc. (cambio mecánico de tipo, ADR 005 punto 9). Sin tocar aserciones.
- Al final no queda ninguno de `controller/ service/ models/ dto/ enums/ repository/ exception/` en la raíz.

Criterios de aceptación:
- `ls src/main/java/dev/racnar/shipbender` → `ShipbenderApplication.java config game player security shared`.
- `grep -rnE "interface I[A-Z][A-Za-z]*(Service|Validation)" src/main/java` → sin salida.
- `grep -rln "Service;\|Validation;" src/main/java | xargs grep -n "import .*\.I[A-Z]"` → sin salida.
- `git diff --cached -M --stat`: solo renombres + cambios de `package`/`import`/`implements`/tipos de campos.
- `./mvnw test` → BUILD SUCCESS, 40 tests, 0 fallos.
- Commit `refactor: paquetes por feature y sin interfaces I*Service (tarea 402)`.

## Resultado
- Movidos con `git mv` los 65 archivos de la tabla; borradas las 8 interfaces (`service/intereface/*` x7 + `IGameValidation`), con `implements`/`@Override` quitados y campos/constructores tipados con la clase concreta. `GameplayService` importaba `intereface.*` (comodin): eliminado.
- `WebSecurityConfig` pasó a `public` (el test `SecurityRulesIntegrationTest` la importa desde otro paquete; no compilaba).
- Tests: solo imports (y tipos `@Mock` en `MatchServiceTest`); aserciones intactas.
- Ambigüedad resuelta por la tabla: `dto/error/ErrorDTO` cayó en `game/dto/` (regla "resto de dto/**"); la 407 lo borra. `config/WebSocketConfig` y `SpringBeans` en `config/`.
- Verificación: raíz = `ShipbenderApplication.java config game player security shared`; sin `interface I*Service/Validation`; `./mvnw test` x2 → 40 tests, 0 fallos, BUILD SUCCESS.
