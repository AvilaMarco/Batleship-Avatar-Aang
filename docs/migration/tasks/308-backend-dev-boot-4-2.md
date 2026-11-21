# 308 · backend-dev · Salto a Spring Boot 4.2.0 (starters modulares, Jackson 3, Security 7, Hibernate 7)
Estado: pendiente
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
