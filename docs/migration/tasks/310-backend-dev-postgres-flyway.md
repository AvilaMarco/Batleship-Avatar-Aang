# 310 · backend-dev · Postgres + Flyway (V1 desde las entidades) y borrar H2
Estado: parcial
Fase: F3
Depende de: 309

Objetivo: el esquema lo crea Flyway y Hibernate solo lo valida; H2 desaparece del proyecto. Ver ADR 003 punto 4.

Archivos: `pom.xml`, `src/main/resources/db/migration/V1__init.sql` (nuevo), `application*.properties`, `config/SpringBeans.java`, `config/WebSecurityConfig.java` (solo quitar `/h2-console/**`). Excepción explícita: borrar en `src/test/.../TestcontainersConfiguration.java` el bean `ddl()` temporal de la 309.

Pasos:
1. Generar el esquema desde las entidades (todavía con el `create-drop` de la 309):
   `./mvnw test -Dtest=AuthIntegrationTest "-Dspring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create" "-Dspring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=target/V1__init.sql"`
   Copia `target/V1__init.sql` a `src/main/resources/db/migration/V1__init.sql`, agrega `;` al final de cada sentencia si faltan y una línea de comentario arriba ("Esquema inicial generado desde las entidades JPA, F3"). Si el archivo sale vacío, escríbelo a mano desde las entidades y el log `create table` de Hibernate.
2. `pom.xml`: agrega `spring-boot-starter-flyway` y `org.flywaydb:flyway-database-postgresql`; `org.postgresql:postgresql` pasa a scope `runtime`; borra `com.h2database:h2`.
3. `application.properties`: borra `spring.datasource.*`, `spring.jpa.database-platform` y `spring.h2.console.*`; agrega `spring.jpa.hibernate.ddl-auto=validate`. `application-dev.properties`: borra `spring.h2.console.enabled=true`. `application-prod.properties`: no se agregan URLs; la conexión llega por las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` (F6). Sin ellas la app no arranca, a propósito.
4. `SpringBeans.initData`: `@Profile("dev")`. Con una base persistente (prod), cada arranque volvería a insertar los datos de ejemplo. Los tests corren con dev y los siguen teniendo.
5. `WebSecurityConfig`: quita `"/h2-console/**"`.
6. Borra el bean `ddl()` temporal de `TestcontainersConfiguration`.

Criterios de aceptación:
- `./mvnw clean test` → BUILD SUCCESS, 30 tests, 0 fallos; en la salida aparece `Successfully applied 1 migration` y ningún error de `validate`.
- `grep -rni "h2" pom.xml src/main` → sin resultados.
- `grep -rn "ddl-auto" src` → solo `ddl-auto=validate` en `application.properties`.
- `PORT=4704 ./mvnw spring-boot:test-run` arranca y la prueba manual de la 307 funciona. Detén el proceso.
- `PORT=4704 ./mvnw spring-boot:run` (sin contenedor ni variables) → no arranca, el error menciona la datasource/URL.

## Resultado

- V1__init.sql generado desde las entidades (9 tablas, 8 FK, checks de enum). Revisado a mano: las entidades no declaran unicidad ni nullable, por eso no hay `unique`/`not null` extra; `validate` pasa.
- pom: flyway starter + flyway-database-postgresql, postgresql a runtime, h2 fuera. Properties sin datasource/H2 y `ddl-auto=validate`; `initData` con `@Profile("dev")`; `/h2-console/**` fuera; bean `ddl()` y DynamicPropertyRegistrar borrados.
- Ajuste extra en `TestcontainersConfiguration`: el contenedor deja de ser static (uno por contexto). Con Flyway un contenedor compartido conservaba los datos de `initData` entre contextos y `MatchControllerIntegrationTest` fallaba con 2 resultados.
- Verificado: `./mvnw clean test` BUILD SUCCESS, 30 tests, 0 fallos, `Successfully applied 1 migration`. `grep ddl-auto src` solo `validate`. `grep -i h2` solo coincide con `<h2>` HTML y imagenes en static (no es H2 DB).
- `PORT=4704 ./mvnw spring-boot:run` sin variables: "Failed to configure a DataSource: 'url' attribute is not specified".
- Pendiente de verificar (chat central): `spring-boot:test-run` en 4704 y prueba manual de dos ventanas.
