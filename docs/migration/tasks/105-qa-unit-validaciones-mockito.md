# 105 · qa · Unit tests: arreglar MatchServiceTest y cubrir GameValidation
Estado: hecha
Fase: F1
Depende de: 101

Objetivo: corregir el mal uso de Mockito y fijar las validaciones de partida que sí se usan. `ShipValidation` no se testea: no lo usa nadie, sus métodos no hacen nada y se borra en la 201. Su ausencia ya queda fijada en el caso 2 de la 104.

Archivos:
- `src/test/java/com/codeblockacademy/shipbender/unit/service/MatchServiceTest.java` (reescribir)
- Nuevo `src/test/java/com/codeblockacademy/shipbender/unit/service/GameValidationTest.java`
- Borrar `src/test/java/com/codeblockacademy/shipbender/unit/service/SalvoServiceTest.java` y `src/test/java/com/codeblockacademy/shipbender/utils/BeansTest.java` (clases vacías)

MatchServiceTest, qué está mal hoy:
- `Authentication authMock = any(Authentication.class)` (línea 50): `any()` es un matcher, devuelve `null` y deja estado colgando en Mockito. Usa `mock(Authentication.class)` o `null` (`PlayerService.getPlayerAuthenticated` ignora el parámetro).
- `thenReturn(any(GamePlayer.class))` (línea 67): devuelve `null`. Devuelve un objeto real o usa `thenAnswer(inv -> inv.getArgument(0))`.
- `lenient()` y el `log.debug` sin aserciones esconden que el test no verifica nada.
Casos: `joinGame` feliz (afirma el `GameCreatedDTO` y haz `verify(gamePlayerService).save(...)`). Partida llena: `gameValidation.isNotFull` lanza `GameIsFullException`, el error se propaga y no se llama a `save`. `createGame` en una location ocupada: lanza `GameAlreadyExistsException` y no se llama a `save`.

GameValidationTest (`GameRepository` mockeado y entidades `Game`/`GamePlayer`/`Player` reales):
- `exists`, `notExists`, `isNotFull`, `notContainsThePlayer`: caso que pasa y caso que lanza cada uno.
- `containsThePlayer` con un jugador que no está → hoy lanza `PlayerAlreadyInGameException` (nombre incorrecto, se fija igual).
- `Game.containsPlayer` con ids `Long` > 127 (usa `new Long`/`Long.valueOf(1000L)` en instancias distintas) → hoy devuelve `false` aunque el jugador esté (`models/Game.java:82` compara con `==`). Fija el comportamiento actual con `// bug conocido: Long con ==, se arregla en F4`. No crees una tarea: el arquitecto ya lo registró para F4.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS y sin warnings `UnnecessaryStubbing` ni `InvalidUseOfMatchers`.
- `grep -nE "= *any\(|thenReturn\(any" -r src/test` → sin resultados.
- `grep -n "lenient" src/test/java/com/codeblockacademy/shipbender/unit/service/MatchServiceTest.java` → sin resultados.
- `git diff --stat -- src/main` → vacío.

## Resultado
MatchServiceTest reescrito (3 casos, sin lenient ni any() mal usado); GameValidationTest nuevo (7 casos); borrados SalvoServiceTest y BeansTest.
`./mvnw test` -> 18 tests, BUILD SUCCESS; greps de aceptacion vacios; `git diff -- src/main` vacio.
Hallazgo: el "bug" de `Game.containsPlayer` (Long con ==) NO se reproduce: `Player.id` es `long` primitivo, el == desempaqueta. El test fija que con id 1000 devuelve true. El arquitecto debe corregir la nota de F4.
Hallazgo: un run (de 3) tuvo 1 fallo en AuthIntegrationTest (flaky, no reproducido); revisar orden/estado compartido.
