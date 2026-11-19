# 103 · qa · Caracterización REST de games y match
Estado: pendiente
Fase: F1
Depende de: 102 (usa `AuthHelper`)

Objetivo: fijar el contrato actual (status + claves de primer nivel del JSON) de los endpoints que usa el front: `GET /api/games` (`main.js:43`), `POST /api/match/games/{nation}/{location}`, `POST /api/match/games/{id}`, `GET /api/match/{id}`, `GET /api/match/{id}/status`.
Fuera de alcance: `/api/games/gp/{id}`, `/api/games/games/players/{gpId}/salvos|ships` y `/api/games/emote/**` de `SalvoController`. Son stubs que el front no usa y se borran en la 201.

Archivos:
- Nuevo `src/test/java/com/codeblockacademy/shipbender/integration/MatchFlowIntegrationTest.java` (`@SpringBootTest @AutoConfigureMockMvc`)

Reglas de datos: cada test crea sus propios jugadores (`AuthHelper.registerAndLogin`) y partidas con una `location` única (p. ej. `"T" + UUID` corto). No dependas de los ids de los datos seed: el contexto de Spring y la H2 se comparten entre clases.

Casos (fija los valores OBSERVADOS):
1. `GET /api/games` sin token y con token de PLAYER → status y claves de primer nivel.
2. A crea una partida → 201, claves observadas (ids de game y game_player).
3. Crear otra partida en la misma `location` → status observado (se espera 409, `GameAlreadyExistsException`).
4. B se une → 200. C se une a la partida llena → status observado (se espera 409).
5. A se une a su propia partida → hoy se acepta (bug: la validación está comentada en `service/MatchService.java:58-59`). Fíjalo y marca el test con el comentario `// bug conocido`.
6. `GET /api/match/{id}` y `/status` con A → 200 y claves; con C, que no está en la partida → status observado.
7. `GET /api/match/{id}/status` con un id inexistente → status observado.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS.
- `./mvnw test -Dtest=MatchFlowIntegrationTest` → 7 tests o más, 0 fallos, y pasa igual dos veces seguidas.
- `git diff --stat -- src/main` → vacío.

## Resultado
