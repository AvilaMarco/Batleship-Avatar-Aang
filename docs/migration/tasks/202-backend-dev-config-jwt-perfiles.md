# 202 · backend-dev · Secreto y expiración JWT en configuración externa; perfiles dev/prod
Estado: pendiente
Fase: F2
Depende de: 201 (toca `JWTAuthorizationFilter` y `pom.xml`) · 102 (los tests ya no usan `ENV_VARIABLES`)

Objetivo: que el código no contenga el secreto JWT, que la expiración sea razonable y configurable, y que prod no exponga la consola H2. Hoy `config/ENV_VARIABLES.java` tiene el secreto en claro y `MINUTES_TOKEN_EXPIRATION = 365241780471L` (unos 700.000 años). El secreto actual ya quedó en el historial de git: se considera comprometido y en prod se usa uno nuevo.

Diseño (cerrado):
- `src/main/resources/application.properties` (común): `spring.profiles.default=dev`, `jwt.expiration-minutes=${JWT_EXPIRATION_MINUTES:1440}` y `spring.h2.console.enabled=false`. Se quita la línea `spring.h2.console.enabled=true`.
- Nuevo `src/main/resources/application-dev.properties`: `jwt.secret=${JWT_SECRET:dev-only-insecure-secret-change-me-0123456789}` y `spring.h2.console.enabled=true`.
- Nuevo `src/main/resources/application-prod.properties`: `jwt.secret=${JWT_SECRET}`, sin valor por defecto, para que la app no arranque sin él.
- `security/JWTUtil.java`: constructor con `@Value("${jwt.secret}") String secret` y `@Value("${jwt.expiration-minutes}") long expirationMinutes`. Construye el `Algorithm.HMAC256` como campo de instancia. Si `secret` tiene menos de 32 caracteres → `IllegalStateException("jwt.secret debe tener al menos 32 caracteres")`.
- `config/ENV_VARIABLES.java`: borrar `SECRET_KEY`, `MINUTES_TOKEN_EXPIRATION` y `ALGORITHM`. Quedan `HEADER`, `PREFIX` y `CLAIMS` (no son secretos; el patrón "interfaz de constantes" se revisa en F4).
- `security/JWTAuthorizationFilter.java`: recibe `JWTUtil` por constructor y usa sus `decodedValidJWT`/`getAuthorities`. Se borran los métodos privados duplicados `decodedValidJWT` y `getAuthorities`.
- `config/WebSecurityConfig.java`: inyecta `JWTUtil` por constructor y hace `new JWTAuthorizationFilter(jwtUtil)`.
- No escribas el valor del secreto viejo en ningún archivo, commit ni en el `## Resultado`.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS, los mismos tests que tras la 201 y 0 fallos (corren con el perfil dev por defecto).
- `grep -rn "SECRET_KEY\|MINUTES_TOKEN_EXPIRATION\|HMAC256" src/main/java` → solo `JWTUtil.java` (`HMAC256`).
- `grep -n "jwt.secret" src/main/resources/application-prod.properties` → exactamente `jwt.secret=${JWT_SECRET}`.
- `./mvnw -q -DskipTests package && java -jar target/shipbender-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod` sin `JWT_SECRET` → no arranca, error que menciona `JWT_SECRET`.
- Con `JWT_SECRET` de 32+ caracteres y `--spring.profiles.active=prod`: `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/h2-console/` → `404`. Con el perfil por defecto (dev) → distinto de `404`.
- Login (`POST /api/players/login`) en dev devuelve un token cuyo `exp` decodificado (jwt.io o `base64 -d` del payload) es unas 24 h después de ahora.

## Resultado
