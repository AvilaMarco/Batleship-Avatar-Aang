# 308 · backend-dev · Salto a Spring Boot 4.2.0 (starters modulares, Jackson 3, Security 7, Hibernate 7)
Estado: parcial
Fase: F3
Depende de: 307

Objetivo: llegar a Boot 4.2.0 (última GA) sin cambiar comportamiento. Ver ADR 003.

Archivos: `pom.xml`, `src/main/java/**`, `src/main/resources/application*.properties`. En `src/test/**` solo imports/anotaciones que mueva la receta (excepción de ADR 003).

Pasos:
1. Receta (llega a 4.0.x):
   `./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0 -Drewrite.exportDatatables=false`
2. A mano, revisando el diff:
   - parent `4.2.0`; `springdoc-openapi-starter-webmvc-ui` `3.2.1`; `java-jwt` `4.6.2`; `modelmapper` `3.2.8`.
   - Starters modulares: `spring-boot-starter-web` → `spring-boot-starter-webmvc`. Si no compila `@AutoConfigureMockMvc`, agrega `spring-boot-starter-webmvc-test` (test). Acepta los starters `*-test` que agregue la receta.
   - Borrar dependencias `webjars-locator-core` y `sockjs-client` (ya no se usan tras la 306/307). `stomp-websocket` 2.3.4 se queda hasta F5.
   - Jackson 3: imports `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*` (`ErrorDTO`, `GameCreatedDTO`). Los de `com.fasterxml.jackson.annotation.*` no cambian. Si algún test falla por diferencias de serialización (orden de propiedades, fechas), agrega `spring.jackson.use-jackson2-defaults=true` en `application.properties` y anótalo en el Resultado.
   - Hibernate 7: en `Player.id` reemplaza `@GeneratedValue(AUTO, generator="native")` + `@GenericGenerator` por `@GeneratedValue(strategy = GenerationType.IDENTITY)`, como el resto de entidades.
   - Security 7: todo en lambda DSL; nada de `authorizeRequests`, `antMatchers` ni `.and()`.
   - La consola H2 deja de existir (en Boot 4 es otro módulo, `spring-boot-h2console`): no lo agregues; H2 se borra en la 310.

Criterios de aceptación:
- `./mvnw clean test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `./mvnw dependency:tree | grep -E "spring-boot:jar:|springdoc-openapi-starter-webmvc-ui|webjars-locator|sockjs"` → `spring-boot` 4.2.0, springdoc 3.2.1, sin webjars-locator ni sockjs.
- `grep -rn "com.fasterxml.jackson.databind\|GenericGenerator\|authorizeRequests\|antMatchers" src/main/java` → sin resultados.
- `PORT=4704 ./mvnw spring-boot:run`: `/swagger-ui/index.html` → `200`; `/webjars/stomp-websocket/2.3.4/stomp.min.js` → `200`; login de la 303 → `200`.
- Prueba manual de la 307 (dos ventanas, crear/unirse/colocar barcos) OK. Detén el proceso.

## Resultado
Estado: parcial. Todo verificado por comando salvo la prueba manual de dos ventanas (pendiente de verificar; la hace el chat central con el navegador).

- Versiones verificadas en Maven Central (maven-metadata): Boot 4.2.0, springdoc 3.2.1, java-jwt 4.6.2, modelmapper 3.2.8.
- La receta dejo Boot 4.0.10 y springdoc 3.0.3: se corrigieron a mano. Del diff de OpenRewrite se descartaron: reindentado del `pom.xml`, `maven-compiler-plugin` con `annotationProcessorPaths` de Lombok (redundante) y el cambio de imports a `mockito.*` en `GameValidationTest`. Se aceptaron: starters `webmvc`, `webmvc-test`, `security-test`, imports `tools.jackson` y `asString()`/`propertyNames()` en tests.
- Cambios a mano: `webjars-locator-lite` y `sockjs-client` borrados del pom (la tarea decia `-core`; lo que habia era `-lite`; `-lite` sigue llegando transitivo desde springdoc, es correcto). `Player.id` -> `IDENTITY`. Se borro `dto/ShipsDTO.java` (archivo 100% comentado que hacia fallar el grep de `GenericGenerator`).
- Causa raiz de un fallo real: Hibernate 7 rechaza JPQL sin `SELECT` con JOIN (`PlayerRepository.rankedPlayer`). Fix: `SELECT p FROM Player p JOIN ...` (mismo resultado, incluye duplicados por join como antes).
- Test (mecanico, sin tocar aserciones): `WebSocketStompClient.connect(String,...)` fue eliminado en Spring 7 -> `connectAsync(...)` (misma firma y retorno).
- No hizo falta `spring.jackson.use-jackson2-defaults=true`: los 30 tests pasan con los defaults de Jackson 3. Ojo: `INDENT_OUTPUT`/`date-format`/`SNAKE_CASE` de application.properties siguen funcionando (login devuelve JSON indentado).
- `./mvnw clean test`: 30 tests, 0 fallos (7+3+1+9+7+3).
- dependency:tree: `spring-boot:jar:4.2.0`, `springdoc-openapi-starter-webmvc-ui:jar:3.2.1`, sin sockjs.
- grep prohibido en `src/main/java`: sin resultados.
- App en PORT=4704 (jar): `/swagger-ui/index.html` 200; `/webjars/stomp-websocket/2.3.4/stomp.min.js` 200; registro 201 y login 200 con token; login invalido 400. Proceso detenido (puerto cerrado, sin java).
- Pendiente de verificar: prueba manual de la 307 (dos ventanas, crear/unirse/colocar barcos).
