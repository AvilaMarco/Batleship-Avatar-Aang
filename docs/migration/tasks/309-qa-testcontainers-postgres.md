# 309 · qa · Tests de integración con Testcontainers (Postgres)
Estado: pendiente
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
