# ADR 005 — Arquitectura backend (F4)

Estado: propuesta · 2026-11-21 · Aplica ADR 002 punto 3 ("Clean Architecture pragmática") e implementa el contrato de ADR 004.

## Decisión
1. **Paquetes por feature** bajo `dev.racnar.shipbender`: `player`, `game`, `chat`, `security`, `config`, `shared` (Anexo A). `match` y `gameplay` se unifican en `game`: `Game` → `GamePlayer` → `Ship`/`Salvo` es un solo agregado; partirlo en dos paquetes crea un ciclo (`Game.placeShips` necesita `Fleet` y `Fleet` necesita `Game`).
2. **Capas dentro de cada feature**: `*Controller` (HTTP/STOMP: DTO ↔ dominio, `Principal` → email) → `*Service` (transacción, repositorios, publicar eventos) → `domain/` (reglas). Repositorios Spring Data usados directamente desde el servicio, sin puertos.
3. **Regla de dependencias**: `domain/` no importa Spring ni Jackson. Se aceptan anotaciones JPA y Lombok en entidades (ADR 002). Se verifica con `grep` (criterio de las tareas), no con ArchUnit. Entre features: `chat → game → player → shared`; `security` no depende de ninguna feature.
4. **Dominio rico** (Anexo B): `Cell`, `ShipType` con tamaño, `Fleet` (valida la flota al construirse), `Game.join / placeShips / participant / opponentOf / status()`, `PlayerStats`. Los servicios dejan de validar.
5. **Estado de la partida derivado** en `Game.status()` (sin columna nueva) + guardas en cada método. **No se usa el patrón State.**
6. **Errores**: un `enum ErrorCode` (código + status HTTP como `int`) y una sola `ShipbenderException(code, detail)`, Java puro. Un `@RestControllerAdvice` (REST) y un `@ControllerAdvice` con `@MessageExceptionHandler` + `@SendToUser("/queue/errors")` (STOMP) traducen a `ProblemDetail`. Se borran `AirbenderException` y sus 13 subclases.
7. **Eventos de dominio → STOMP** con `ApplicationEventPublisher` en los servicios y un único `@TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)` que hace `convertAndSend("/topic/games/{id}")`.
8. **Se borran**: las 7 interfaces `I*Service` + `IGameValidation` (una implementación cada una), ModelMapper (DTOs `record` con `static from(...)`), `ENV_VARIABLES` (antipatrón *Constant Interface*), código comentado.
9. **Excepción de rol** (como ADR 003 punto 3): en 401 y 402 backend-dev mueve `src/test` (paquetes, imports, tipos de `@Mock`) sin tocar aserciones.
10. **Proceso de contrato: test primero.** qa reescribe los tests de caracterización al contrato v2 (quedan en rojo) y backend-dev implementa hasta ponerlos en verde. backend-dev no puede tocar tests y el contrato viejo se rompe a propósito: así su criterio sigue siendo "`./mvnw test` verde" y no "verde salvo esta lista".

## Alternativas descartadas
- **Patrón State (GoF)**: 3 estados y 2 acciones (`join`, `placeShips`; `fire` en v2.1); una clase por estado y un conversor JPA para 6 guardas de una línea. Se reconsidera si v2.1 agrega estados con comportamiento propio (p. ej. pausa o reconexión).
- **Columna `status` persistida**: dos fuentes de verdad (columna y datos). Hoy el estado sale de los datos (`Game.java:65-73`); solo se ordena y se saca del DTO.
- **Hexagonal / dominio separado de JPA**: ya descartado en ADR 001/002.
- **`AbstractAggregateRoot` de Spring Data** (la entidad registra sus eventos): mete Spring en el dominio.
- **Mandar a STOMP desde el controller** después del servicio: cero clases nuevas, pero el controller decide qué pasó (si la partida arrancó) y eso es regla de negocio fuera de su capa.
- **MapStruct en vez de ModelMapper**: con ~10 DTOs, un `from()` escrito a mano es más corto y se ve qué campos salen.
- **Spring Modulith / ArchUnit**: no hace falta para 6 paquetes; `grep` alcanza.

## Consecuencias
- Varias tareas seguidas dejan la rama en rojo a propósito (la de qa pone rojo, la de backend-dev siguiente lo pone en verde). Merge a `main` solo en verde.
- Los tests unitarios del dominio corren sin Docker ni Spring.

---

## Anexo A · Estructura objetivo
```
dev/racnar/shipbender/
  ShipbenderApplication.java                  (ex SalvoApplication)
  config/    WebSocketConfig, DevDataSeeder (ex SpringBeans.initData, perfil dev)
  security/  WebSecurityConfig, WebSocketSecurityConfig, JwtService (ex JWTUtil),
             JwtAuthenticationFilter, StompAuthInterceptor, ProblemAuthenticationEntryPoint, PasswordConfig
  shared/    NationType
  shared/error/  ErrorCode, ShipbenderException           (Java puro)
                 RestExceptionHandler, StompExceptionHandler (Spring)
  player/    PlayerController, AuthController, PlayerService, AuthService (ex SessionService),
             PlayerRepository, dto/ (records)
  player/domain/  Player (entidad), Rol, PlayerStats
  game/      GameController (incluye POST ships), GameService, GameRepository, dto/ (records)
  game/domain/    Game, GamePlayer, Ship, Salvo (entidades), GameStatus, ShipType, Cell, ShipPlacement, Fleet
  game/event/     GameEvent (sealed interface + records), GameEventStompPublisher (listener)
  chat/      EmoteController (@MessageMapping), ChatService, dto/EmoteRequest
```
Se van: `GamePlayerRepository` (se navega desde `Game`), `ShipRepository` y `SalvoRepository` (sin uso), `PlayerStatus`, `GameDTO`, `CreatedShipsDTO`, `EmoteDTO`, `SalvoDTO`, `GameDataDTO`, `MenuViewDTO`, `StatusGameDTO` y los demás DTOs v1 reemplazados por records.

Servicios: `SalvoService` (registro, nación y menú: nombre que miente) + `PlayerService` + `SessionService` → `PlayerService` + `AuthService`. `MatchService` + `GameplayService` + `GameService` + `GamePlayerService` + `GameValidation` → `GameService`.

## Anexo B · API del dominio (firmas congeladas: qa testea contra esto)
```java
// game/domain — sin Spring
public record Cell(char row, int col) {           // A..J, 1..10
    public static Cell parse(String s);            // "B7"; si no cumple → ShipbenderException(INVALID_FLEET, "celda inválida: K1")
    @Override public String toString();            // "B7"
}
public enum ShipType { CARRIER(5), BATTLESHIP(4), SUBMARINE(3), DESTROYER(3), PATROL_BOAT(2); public int size(); }
public record ShipPlacement(ShipType type, List<Cell> cells) {}
public record Fleet(List<ShipPlacement> ships) {}  // el constructor compacto valida ADR 004 A.4 → INVALID_FLEET (detail = primera regla rota)
// Game (entidad)
public GamePlayer join(Player player);             // ALREADY_IN_GAME, GAME_FULL; agrega a gamePlayers
public void placeShips(long playerId, Fleet fleet);// NOT_A_PARTICIPANT, SHIPS_ALREADY_PLACED; crea los Ship
public GamePlayer participant(long playerId);      // NOT_A_PARTICIPANT
public Optional<GamePlayer> opponentOf(long playerId);
public GameStatus status();                        // finishDate ≠ null → FINISH; <2 jugadores o alguno sin barcos → CREATED; si no IN_GAME
// player/domain
public record PlayerStats(int score, int won, int tied, int lost, double winRate) {
    public static PlayerStats of(List<Integer> scores);   // 3 gana, 1 empata, 0 pierde; winRate = won*100.0/total
}
// shared/error
public enum ErrorCode { VALIDATION_FAILED(400), ..., INTERNAL_ERROR(500); public int status(); }  // lista en ADR 004 Anexo C
public class ShipbenderException extends RuntimeException { public ShipbenderException(ErrorCode code, String detail); public ErrorCode code(); }
```

## Anexo C · Cada decisión y el código actual que la motiva
| Decisión | Motivación (código actual, paquete `com.codeblockacademy.shipbender`) |
|---|---|
| Dominio rico: `Fleet` | `GameplayService.java:44-47` copia los barcos del cliente con ModelMapper sin validar nada; `ShipType.java:11` `@JsonEnumDefaultValue NONAME` acepta cualquier tipo. |
| Dominio rico: `Game.join` | `MatchService.java:58-60` valida "llena" en un servicio y deja comentada la regla "ya estás"; `GameValidation` (`service/validations`) es un servicio Spring para 5 `if`. |
| Estado derivado ordenado | `Game.java:48-73`: la entidad recibe `StatusGameDTO.StatusGameDTOBuilder` (el dominio depende de un DTO). `Game.java:104-112`: host/cliente por índice 0/1 de una lista `@OneToMany` sin `@OrderBy` (orden no garantizado). `GameService.java:68` devuelve `null` si terminó. |
| Vista por quien pregunta (`me`/`opponent`) | `GameService.java:39-46` "oculta" los barcos del rival vaciando colecciones de entidades administradas (`gp.emptyShips()`); dentro de la transacción de `createShips` eso podría llegar a la BD y borrar los barcos del rival al hacer flush (**sospecha, a verificar en 408**). |
| `PlayerStats` en dominio | `PlayerService.java:104`: `(double) (stats.getWon() / scores.size())` divide enteros: el win rate es siempre 0 o 100. |
| `ErrorCode` + 1 excepción pura | `AirbenderException.java:10` lleva `HttpStatus` (el dominio depende de Spring Web); 14 clases para 9 mensajes; `LoginException extends NotFoundException` → 404 en login. |
| Identidad explícita (`Principal` → email) | `PlayerService.java:50-55` y `GameplayService.java:54` ignoran el `Authentication` que reciben y leen `SecurityContextHolder`. |
| Eventos after-commit | `GameplayController.java:32` publica con `@SendTo` el resultado del método: decide el destino el controller y no hay forma de mandar un evento público y otro privado. |
| Borrar `I*Service` | `service/intereface/*` (7) + `IGameValidation`: una implementación cada una; Mockito mockea clases. |
| Borrar ModelMapper | `GamePlayerDTO`/`PlayerDTO` sacan el email de todos en `GET /api/games`; `mapper.map` copia `salvosImpacted` que manda el cliente. |
| `@Payload` + DTO en STOMP | `GameplayController.java:33,40` usan `@RequestBody` (anotación HTTP) y el emote llega como `String` crudo. |
