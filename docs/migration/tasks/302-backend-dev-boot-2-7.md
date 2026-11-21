# 302 · backend-dev · Salto a Spring Boot 2.7.18 + SecurityFilterChain
Estado: pendiente
Fase: F3
Depende de: 301

Objetivo: pasar a Boot 2.7.18 (la última 2.x) y reemplazar `WebSecurityConfigurerAdapter`, que en 2.7 está deprecado y en 3.0 se borra. Las reglas de acceso no cambian (el deny-by-default va en la 311). Ver ADR 003.

Archivos: `pom.xml`, `config/WebSecurityConfig.java` (y lo que toque la receta en `src/main/java`).

Pasos:
1. Receta (sin agregar el plugin al pom):
   `./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 -Drewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 -Drewrite.exportDatatables=false`
2. Revisa el diff y deja estas versiones fijas: parent `2.7.18`; `springdoc-openapi-ui` `1.8.0`. No cambies `java.version` (sigue en 17; Java 21 es la 304).
3. `WebSecurityConfig`: deja de extender `WebSecurityConfigurerAdapter`; pasa a `@Bean SecurityFilterChain filterChain(HttpSecurity http)` con exactamente las mismas reglas, el mismo filtro JWT y el mismo logout. Si la receta ya lo convirtió, revisa que esté así. Sigue con `antMatchers` (Security 5.7 no tiene `requestMatchers(String)`).
4. No toques `WebSocketSecurityConfig` (en 2.7 no está deprecado; se migra en la 305).

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos.
- `grep -rn "WebSecurityConfigurerAdapter" src/main/java` → sin resultados.
- `grep -n "<version>2.7.18</version>\|<version>1.8.0</version>" pom.xml` → 2 líneas.
- `./mvnw -q -DskipTests compile 2>&1 | grep -i "deprecat"`: lista en el Resultado las deprecaciones que queden (son pistas para la 303).
- `PORT=4704 ./mvnw spring-boot:run` arranca y `/swagger-ui/index.html` → `200`. Detén el proceso.

## Resultado
