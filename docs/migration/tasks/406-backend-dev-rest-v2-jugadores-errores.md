# 406 · backend-dev · REST v2: auth, jugadores y formato de error único
Estado: borrador
Fase: F4
Depende de: 403, 405
Paralelizable: sí con 404 (qa, `src/test/unit`); no con 407 (comparten `WebSecurityConfig` y `shared/error`)

Objetivo: implementar ADR 004 Anexo A.1, el formato de error del Anexo C y el 401 de seguridad, hasta poner en verde `AuthIntegrationTest` y `SecurityRulesIntegrationTest` de la 405.

Archivos (`P = src/main/java/dev/racnar/shipbender`): `P/player/**`, `P/security/**`, `P/shared/error/RestExceptionHandler.java` (reemplaza `ErrorHandlerController`), `src/main/resources/db/migration/V2__player_email_unique.sql`.

Diseño (cerrado):
- `AuthController` `POST /api/auth/login` → `AuthService.login(email, password)` (ex `SessionService`): fallo → `ShipbenderException(INVALID_CREDENTIALS)`.
- `PlayerController`: `POST /api/players`, `GET /api/players/me`, `PATCH /api/players/me`, `GET /api/players/ranking`. Los controllers reciben `Principal` y pasan `principal.getName()` (email) al servicio. `PlayerService` no lee `SecurityContextHolder` ni recibe `Authentication`.
- `PlayerService` absorbe `registerPlayer` y `setNationPlayer` de `SalvoService`; `getInfoGames` y `SalvoController` (`GET /api/games` v1) quedan hasta la 407, que los borra.
- DTOs `record` en `player/dto/` con `static from(...)`: `LoginRequest`, `TokenResponse(email, token)`, `RegisterRequest` (mismas validaciones que `SignInPlayerDTO`), `UpdatePlayerRequest(@NotNull NationType nation)`, `PlayerProfile`, `PlayerSummary`, `RankingEntry`, `StatsResponse`. Stats con `PlayerStats.of(player.getScores())`. Borrar los DTOs v1 de `player/dto` que queden sin uso; no usar ModelMapper en código nuevo.
- Ranking: una entrada por jugador (`select distinct` o deduplicar), orden `score` desc.
- `RestExceptionHandler` (`@RestControllerAdvice`) → `ProblemDetail` con propiedades `code` y, en validación, `errors` (`Map<String, List<String>>`): `ShipbenderException` → su status; `MethodArgumentNotValidException` → `VALIDATION_FAILED`; `HttpMessageNotReadableException` → `MALFORMED_REQUEST`; `AirbenderException` (vieja, hasta 407) → su status con `code` = `INTERNAL_ERROR` si no tiene equivalente; `Exception` → 500 `INTERNAL_ERROR` sin detalle interno.
- Seguridad (`WebSecurityConfig`): `POST /api/auth/login` y `POST /api/players` públicas; `/api/**` → `hasAuthority("PLAYER")` (GUEST deja de usarse; el valor del enum queda); estáticos, handshake, swagger y `/error` igual; `anyRequest().denyAll()`. Quitar `.logout(...)`. `exceptionHandling`: entry point → 401 `UNAUTHENTICATED` y access denied → 403 `FORBIDDEN`, ambos escribiendo `ProblemDetail` JSON con el `ObjectMapper`/`JsonMapper` del contexto.
- `JwtAuthenticationFilter` (ex `JWTAuthorizationFilter`): si `verify` lanza `JWTVerificationException`, limpiar el contexto y seguir la cadena (termina en 401). Renombrar `JWTUtil` → `JwtService`. `ENV_VARIABLES` → constantes `private static final` en `JwtService` (y que filtro e interceptor las lean de ahí).
- `V2__player_email_unique.sql`: `alter table player add constraint uk_player_email unique (email);`.

Criterios de aceptación:
- `./mvnw test`: `AuthIntegrationTest` y `SecurityRulesIntegrationTest` en verde; unitarios en verde; solo fallan los de `GameRestIntegrationTest` listados en el Resultado de la 405.
- `grep -rn "SecurityContextHolder\|ModelMapper\|ENV_VARIABLES" src/main/java/dev/racnar/shipbender/player src/main/java/dev/racnar/shipbender/security` → solo `SecurityContextHolder` en el filtro JWT.
- `curl -s -X POST localhost:4704/api/auth/login -H 'Content-Type: application/json' -d '{"email":"marco@admin.com","password":"x"}'` (con `PORT=4704 ./mvnw spring-boot:test-run`) → `"code" : "INVALID_CREDENTIALS"` y status 401. Detener el proceso.
- Commit `feat: REST v2 de auth y jugadores con ProblemDetail (tarea 406)`.

## Resultado
