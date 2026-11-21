# 309 · qa · Tests de integración con Testcontainers (Postgres)
Estado: parcial
Fase: F3
Depende de: 308

Objetivo: que los tests de integración corran contra Postgres real en un contenedor, con un solo contenedor por corrida, y que el mismo setup sirva para levantar la app en dev (`spring-boot:test-run`). Ver ADR 002 punto 2 y ADR 003 punto 4. Requiere Docker corriendo.

Archivos: `pom.xml` (solo dependencias test), `src/test/java/com/codeblockacademy/shipbender/TestcontainersConfiguration.java` (nuevo), `src/test/java/com/codeblockacademy/shipbender/TestShipbenderApplication.java` (nuevo), las 4 clases de `integration/`.

Diseño (cerrado):
- Dependencias `test` (versiones gestionadas por Boot 4.2): `org.springframework.boot:spring-boot-testcontainers`, `org.testcontainers:testcontainers-postgresql`, `org.testcontainers:testcontainers-junit-jupiter` y `org.postgresql:postgresql` (scope `test` por ahora; la 310 lo pasa a `runtime`).
- `TestcontainersConfiguration`: `@TestConfiguration(proxyBeanMethods = false)` con `@Bean @ServiceConnection PostgreSQLContainer postgres()` → `new PostgreSQLContainer("postgres:17-alpine")`. Mismo tag que usará F6.
- Temporal hasta la 310 (todavía no hay Flyway): en la misma clase, `@Bean DynamicPropertyRegistrar ddl()` que registra `spring.jpa.hibernate.ddl-auto=create-drop`, con el comentario `// temporal: lo borra la tarea 310 (Flyway)`.
- Las 4 clases de `integration/`: `@Import(TestcontainersConfiguration.class)`. Misma config → Spring reutiliza el contexto y el contenedor.
- `TestShipbenderApplication`: `main` con `SpringApplication.from(SalvoApplication::main).with(TestcontainersConfiguration.class).run(args)`.
- No toques aserciones ni el código de producción. H2 sigue en el classpath (lo borra la 310), pero ya no se usa en estos tests.

Criterios de aceptación:
- Con Docker corriendo: `./mvnw test` → BUILD SUCCESS, 30 tests, 0 fallos; en la salida aparece `postgres:17-alpine` y `Container ... started` una sola vez.
- `./mvnw test 2>&1 | grep -c "jdbc:h2"` → `0`.
- Con Docker detenido: `./mvnw test` falla con un mensaje que menciona Docker (anota el texto en el Resultado; es el comportamiento esperado según ADR 002).
- `PORT=4704 ./mvnw spring-boot:test-run` arranca la app contra Postgres en contenedor; login de la 303 → `200`. Detén el proceso.

## Resultado
- `./mvnw test` con Docker: BUILD SUCCESS, 30 tests, 0 fallos. `postgres:17-alpine started` 1 vez (más el de ryuk), `grep -c jdbc:h2` = 0.
- `PORT=4704 ./mvnw spring-boot:test-run`: arrancó contra Postgres en contenedor (`jdbc:postgresql://localhost:63098/test`); `POST /api/players/login` con marco@admin.com / 123 -> 200 (el seed corre sobre Postgres). Proceso detenido, ryuk limpió los contenedores; `docker ps` vacío, sin java.
- Desvíos del diseño (ambos solo en `TestcontainersConfiguration`):
  1. `application.properties` fija `spring.jpa.database-platform=H2Dialect`, lo que generaba DDL de H2 (`tinyint`, `enum`) y rompía 20 tests en Postgres. El `DynamicPropertyRegistrar` temporal también pisa el dialecto con `PostgreSQLDialect`. Se va con la 310 (conviene que la 310 borre esa línea de producción).
  2. El contenedor como bean no basta: `GameplayWebSocketTest` (RANDOM_PORT) crea otro contexto y arrancaba un segundo Postgres. El contenedor es ahora un `static final` compartido -> un solo contenedor por corrida.
- NO VERIFICADO: criterio "con Docker detenido" (no se detuvo Docker Desktop por indicación); el texto del error queda para que lo confirme el usuario.
