# ADR 003 — Upgrade a Spring Boot 4.2 / Java 21 (F3)

Estado: propuesta · 2026-11-21

## Contexto
Hoy: Boot 2.6.6, Java 17, H2, SockJS, `WebSecurityConfigurerAdapter` con `anyRequest().permitAll()`. Versiones según Maven Central el 2026-11-21 (sin WebSearch): Boot 4.2.0 (última GA; 4.1.3 la anterior), 3.5.16 y 2.7.18 (últimas de su línea), springdoc 3.2.1 (compilado contra Boot 4.2.0), rewrite-maven-plugin 6.46.1 + rewrite-spring 6.37.1 (receta hasta `UpgradeSpringBoot_4_0`).

## Decisión
1. **Destino Boot 4.2.0 + Java 21.** Saltos con `./mvnw test` verde (30 tests) entre cada uno: 2.6.6 → 2.7.18 → 3.5.16 → Java 21 → 4.2.0. Java 21 va después de 3.5 (byte-buddy de Boot 2.7 no soporta class files 65).
2. **Qué rompe por salto en este proyecto:** 2.7: `WebSecurityConfigurerAdapter` deprecado → `SecurityFilterChain`; springdoc 1.8.0. 3.5: `javax`→`jakarta` (11 archivos), Security 6 (`authorizeHttpRequests`, `requestMatchers`, lambda DSL), Hibernate 6.6, springdoc → `springdoc-openapi-starter-webmvc-ui` 2.8.17, `LocalServerPort` cambia de paquete, Lombok lo gestiona Boot. 4.2: starters modulares (`spring-boot-starter-webmvc`, `*-test`), Jackson 3 (`tools.jackson.databind`), Security 7 (se borra `AbstractSecurityWebSocketMessageBrokerConfigurer`), Hibernate 7 (`@GenericGenerator` fuera), Spring 7 sin resolver de `webjars-locator-core`, springdoc 3.2.1. `mysql-connector-java` y el plugin `springdoc-openapi-maven-plugin` (arrancaba la app en `integration-test`) se eliminan antes del primer salto; nadie usa el `openapi.json` generado y Swagger UI sigue en `/swagger`. java-jwt 4.6.2 y ModelMapper 3.2.8 solo suben de versión.
3. **OpenRewrite + revisión manual.** La receta (`UpgradeSpringBoot_2_7` / `_3_5` / `_4_0`) se corre por línea de comandos, sin agregarla al `pom.xml`, y hace lo mecánico (imports, propiedades, starters). Seguridad, WebSocket y persistencia se hacen a mano en tareas propias porque cambian comportamiento. Si la receta falla, se aplica a mano la lista de la tarea. Excepción acotada de rol: en los saltos backend-dev puede aplicar en `src/test` cambios mecánicos de imports/anotaciones, nunca de aserciones.
4. **Postgres + Flyway, H2 se elimina** (reemplaza "H2 solo para tests" de ADR 001). Tests de integración y dev local con Testcontainers (`@ServiceConnection`, `./mvnw spring-boot:test-run`); no hace falta compose de dev en F3. `ddl-auto=validate`; esquema `V1__init.sql` generado desde las entidades.
5. **WebSocket nativo, sin SockJS**, heartbeats STOMP 10 s/10 s (Cloudflare corta conexiones ociosas). El front viejo sigue funcionando en F3 (3 líneas: `Stomp.client` + ruta versionada del webjar). Así cada salto se puede probar a mano.
6. **Deny-by-default:** `anyRequest().denyAll()` con lista explícita de rutas públicas (tarea 311).

## Alternativas descartadas
- Ir directo a 4.2: si algo se rompe, no se sabe en qué salto. Quedarse en 4.1.3: 4.2.0 ya tiene springdoc compatible.
- Solo manual: más lento y es fácil olvidarse de propiedades renombradas. Solo receta: no decide reglas de seguridad.
- Compose de Postgres para dev ahora: duplica lo que hace F6; Testcontainers ya da una base efímera, igual que H2 hoy.

## Consecuencias
- `./mvnw test` y `spring-boot:test-run` requieren Docker corriendo. `./mvnw spring-boot:run` sin `SPRING_DATASOURCE_URL` no arranca (a propósito).
- La consola H2 desaparece al pasar a Boot 4 (tarea 308) y no se repone.
