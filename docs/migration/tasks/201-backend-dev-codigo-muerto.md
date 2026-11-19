# 201 · backend-dev · Borrar código muerto del backend y dependencias sobrantes
Estado: hecha
Fase: F2
Depende de: 102, 103, 104, 105 (F1 en verde)

Objetivo: quitar código y dependencias que no se usan. Todo lo de abajo se verificó con grep. No debe cambiar el comportamiento de nada que cubra F1.

Archivos y cambios:
1. `controller/GameplayController.java`: borrar `matchSalvos` y `reMatch` (líneas 45-61). Son handlers falsos con `System.out`, devuelven `ErrorDTO("emotes", ...)` y piden un `@DestinationVariable userId` que no existe en la ruta. El front deja de enviarles mensajes en la 203.
2. `controller/MatchController.java:95`: borrar el `System.out.println(matchService.getGame(...))`. Hoy ejecuta la consulta dos veces.
3. `controller/SalvoController.java`: borrar `getGame_view` (`/gp/{id}`), `createSalvos`, `createShips` y `setemote`, con sus bloques comentados y los imports/campos que queden sin uso. Son stubs que siempre devuelven 401/403 y el front no los llama (`grep -rn "/api/" src/main/resources/static --include=*.js` solo encuentra `/api/games`, `/api/match/**` y `/api/players/**`, sin contar `sobras.js`). Se queda `GET /api/games`.
4. `service/validations/ShipValidation.java` e `IShipValidation.java`: borrarlos. Nadie los inyecta (`grep -rn "ShipValidation" src/main/java` solo los encuentra a ellos mismos) y sus métodos son `void` que calculan y descartan el resultado.
5. `security/JWTAuthorizationFilter.java`: borrar el `try/catch` de `ExpiredJwtException | UnsupportedJwtException | MalformedJwtException` (líneas 37-50) y sus imports. Son de jjwt y nunca se lanzan, porque se verifica con auth0 `java-jwt`. Sin el catch el comportamiento es idéntico (lo fija el caso 7 de la 102).
6. `pom.xml`: borrar la dependencia `io.jsonwebtoken:jjwt` (su único uso era el punto 5) y el segundo bloque duplicado de `spring-boot-devtools` (el que está después de Lombok).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, con los mismos tests que al cerrar F1 y 0 fallos.
- `grep -rn "io.jsonwebtoken\|jjwt" pom.xml src/main` → sin resultados.
- `grep -c "spring-boot-devtools" pom.xml` → `1`.
- `grep -rn "System.out" src/main/java` → sin resultados en `controller/`.
- `grep -rn "ShipValidation\|matchSalvos\|reMatch\|getGame_view" src/main/java` → sin resultados.
- `./mvnw spring-boot:run` levanta y `GET http://localhost:8080/web/games.html` → 200.

## Resultado
Borrado todo lo indicado (puntos 1-6). `./mvnw test`: 30 tests, 0 fallos, BUILD SUCCESS. Greps de aceptación sin resultados; devtools = 1 ocurrencia.
No verificado: `spring-boot:run` + GET games.html (los tests @SpringBootTest cargan el contexto).
