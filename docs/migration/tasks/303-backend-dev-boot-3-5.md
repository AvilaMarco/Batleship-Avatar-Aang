# 303 · backend-dev · Salto a Spring Boot 3.5.16 (jakarta, Security 6, Hibernate 6)
Estado: pendiente
Fase: F3
Depende de: 302

Objetivo: pasar a Boot 3.5.16 (la última 3.x) sin cambiar comportamiento. Ver ADR 003.

Archivos: `pom.xml`, `src/main/java/**`, `src/main/resources/application*.properties`. En `src/test/**` solo cambios mecánicos de imports que haga la receta (excepción de ADR 003); ninguna aserción.

Pasos:
1. Receta:
   `./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 -Drewrite.exportDatatables=false`
2. Revisa el diff y deja esto (si la receta no lo hizo, hazlo a mano):
   - parent `3.5.16`; `java.version` sigue en `17`.
   - `javax.servlet|persistence|validation` → `jakarta.*` (controller, dto, models, `JWTAuthorizationFilter`).
   - `springdoc-openapi-ui` → `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`. Las propiedades `springdoc.*` no cambian.
   - Borrar la propiedad `lombok.version` (Boot 3.5 gestiona una versión compatible con JDK 21).
   - `WebSecurityConfig` en lambda DSL: `authorizeHttpRequests(auth -> auth.requestMatchers(...)...)`, `csrf(c -> c.disable())`, `logout(l -> l.logoutUrl(...).logoutSuccessHandler(...))`. Mismas reglas.
   - Test: el import de `LocalServerPort` pasa a `org.springframework.boot.test.web.server.LocalServerPort`.
3. Si al arrancar aparece `This method cannot decide whether these patterns are Spring MVC patterns or not` (ocurre por el servlet de la consola H2): `"/h2-console/**"` → `PathRequest.toH2Console()` y el resto de patrones con `PathPatternRequestMatcher.withDefaults().matcher(...)`.
4. `WebSocketSecurityConfig` queda deprecado pero compila: no lo toques (se migra en la 305).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `grep -rn "import javax\.\(servlet\|persistence\|validation\)" src` → sin resultados.
- `grep -n "springdoc-openapi-ui\|lombok.version\|WebSecurityConfigurerAdapter" pom.xml src -r` → sin resultados.
- `PORT=4704 ./mvnw spring-boot:run`: arranca; `/swagger-ui/index.html` → `200`; `curl -s -X POST -H "Content-Type: application/json" -d '{"email":"marco@admin.com","password":"<la del seed en SpringBeans>"}' http://localhost:4704/api/players/login` → `200` con token. Detén el proceso.
- Prueba manual en el navegador (`http://localhost:4704/`): login, crear partida y unirse con otro usuario en otra ventana; se ve la partida (WebSocket con SockJS sigue funcionando). Anota el resultado.

## Resultado
