# 102 · qa · Caracterización de auth: login, registro y JWT
Estado: hecha
Fase: F1
Depende de: 101

Objetivo: fijar el comportamiento actual de login/registro y del filtro JWT, y dejar un helper que obtenga tokens por la API real. Así los tests no dependen de `config/ENV_VARIABLES` (en la tarea 202 el secreto sale de ahí y los tests no deben romperse).

Archivos:
- Nuevo `src/test/java/com/codeblockacademy/shipbender/integration/support/AuthHelper.java`: `static String loginToken(MockMvc mvc, String email, String password)` hace `POST /api/players/login` y devuelve el campo `token` (ya trae el prefijo `Bearer `). También `static String registerAndLogin(MockMvc mvc)`: registra un usuario con email único (`UUID`) por `POST /api/players` y devuelve su token (al registrarse recibe el rol PLAYER, `SalvoService.java:53`).
- Nuevo `src/test/java/com/codeblockacademy/shipbender/integration/AuthIntegrationTest.java` (`@SpringBootTest @AutoConfigureMockMvc`)
- Reescribir `src/test/java/com/codeblockacademy/shipbender/integration/MatchControllerIntegrationTest.java`: borrar el test `aaa` (no prueba nada), cambiar el `@BeforeAll` que firma con `ENV_VARIABLES.ALGORITHM` por `@BeforeEach` con `AuthHelper.loginToken` (usuario seed `marco@admin.com`; la contraseña está en `config/SpringBeans.java`) y renombrar `aaabbb` a un nombre que describa el comportamiento.

Casos (fija el status y los campos que OBSERVES; JSON en snake_case):
1. Login con un usuario seed válido → 200, `email` y `token` que empieza con `Bearer `.
2. Login con contraseña incorrecta → status observado (`LoginException` extiende `NotFoundException`, se espera 404).
3. Login con un email mal formado → 400 y `error_fields.email` presente.
4. Registro con un email nuevo → 201; el mismo email otra vez → status observado (se espera 409).
5. `POST /api/players/nation/FIRE` con el token de (4) → 200.
6. El mismo endpoint sin header `Authorization` → status observado.
7. El mismo endpoint con un token de firma inválida (cambia el último carácter) → comportamiento observado. Si MockMvc lanza la excepción en vez de responder, fíjalo con `assertThrows` (hoy el filtro solo atrapa excepciones de jjwt, que nunca se lanzan: `security/JWTAuthorizationFilter.java:47`).
- No afirmes el valor exacto de la expiración del token: cambia en la 202.

Criterios de aceptación:
- `./mvnw test` → BUILD SUCCESS.
- `./mvnw test -Dtest=AuthIntegrationTest` → 7 tests o más, 0 fallos.
- `grep -rn "ENV_VARIABLES" src/test` → sin resultados.
- `git diff --stat -- src/main` → vacío.

## Resultado
Creados `AuthHelper`, `AuthIntegrationTest` (7 tests) y reescrito `MatchControllerIntegrationTest` (sin ENV_VARIABLES). Estados observados: password incorrecta 404, email repetido 409, sin Authorization 403, firma inválida: MockMvc lanza excepción (fijado con assertThrows). El email admite max 30 chars (`@Size(max=30)`), por eso el helper usa un UUID recortado. `./mvnw test` verde (9 tests); `src/main` sin cambios.
