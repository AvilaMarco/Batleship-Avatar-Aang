# Registro de aprendizaje

Errores del proyecto original y cómo se mejoraron, con código real. Prioridad: arquitectura y patrones de diseño.
Lo edita el chat central con las notas que devuelve cada rol. Al cerrar cada fase se explican las entradas de prioridad alta.

Formato de entrada:
- **Antes**: archivo:línea (y fragmento si ayuda)
- **Problema**: por qué estaba mal o se podía mejorar
- **Después**: cómo quedó (archivo:línea / fragmento)
- **Por qué es mejor**
- **Concepto**: patrón o principio
- **Prioridad**: alta (arquitectura/diseño) · media · baja

---

## F0 · Preparación

### Documentar el dominio para humanos y para IA
- **Antes**: el contexto del proyecto vivía en tu cabeza y en `readme.md` (instrucciones de juego, link a Heroku caído).
- **Problema**: cualquier persona (o IA) que entra tiene que releer el código para entender qué es un GamePlayer o en qué estado está la migración. Eso cuesta tiempo y, con IA, tokens en cada sesión.
- **Después**: `CLAUDE.md` (glosario + comandos + reglas), `docs/migration/PLAN.md` (estado), `docs/adr/` (decisiones con su porqué).
- **Por qué es mejor**: el conocimiento queda en el repo, versionado; las decisiones no se vuelven a discutir porque está escrito por qué se tomaron.
- **Concepto**: Architecture Decision Records (ADR), lenguaje ubicuo (DDD).
- **Prioridad**: media

### Observaciones detectadas para fases siguientes (se completan al corregirlas)
- Secreto JWT en el código (`config/ENV_VARIABLES.java`) y expiración absurda → F2. Concepto: configuración externa (12-factor).
- `WebSecurityConfig`: `anyRequest().permitAll()` deja abierto todo lo no listado → F3. Concepto: deny by default.
- Lógica de juego en controllers (`controller/SalvoController.java`, 283 líneas) → F4. Concepto: SRP, modelo de dominio rico.
- Interfaces `I*Service` con una sola implementación → F4. Concepto: YAGNI vs. inversión de dependencias bien usada.
- `unit/service/MatchServiceTest.java`: `any()` de Mockito usado como valor (`Authentication authMock = any(...)`, `thenReturn(any(...))`) → F1. Concepto: matchers vs. valores en mocks.
- Código muerto (`static/web/js/game-mjs/sobras.js`, `static/web/delete/`) → F2.
- Build no reproducible: falta `.mvn/wrapper/` y Lombok no compila en JDK 21 → F1 (101). Concepto: build reproducible.
- `service/validations/ShipValidation.java`: métodos `void` que calculan un booleano y lo descartan; nadie los inyecta → el servidor acepta cualquier barco → F2/F4. Concepto: validar en el límite de confianza, fail fast.
- `models/Game.java:82`: parecía `Long == Long`, pero `Player.id` es `long` primitivo y el test (F1/105) demostró que NO falla. Corrección de una nota mía: verificar antes de afirmar. `GameService.java:41` (`gp.getId() != gamePlayerId`) tampoco es bug (long contra Long desempaqueta; `.equals` ni compila). Lección repetida: verificar la premisa de un bug antes de arreglarlo. Concepto: identidad vs. igualdad.
- `security/JWTAuthorizationFilter.java:47`: atrapa excepciones de jjwt pero verifica con auth0, y duplica `JWTUtil` → F2. Concepto: DRY, una librería por responsabilidad.
- `ENV_VARIABLES.java`: interfaz de constantes implementada por 3 clases → F2. Concepto: antipatrón *Constant Interface*.
- `GameValidation.containsThePlayer` lanza `PlayerAlreadyInGameException` cuando el jugador NO está; `PlayerService.getPlayerAuthenticated` ignora su parámetro y lee `SecurityContextHolder` → F4. Concepto: nombres honestos, dependencias explícitas.
- `service/MatchService.java:58`: validación "ya está en la partida" comentada → un jugador se puede unir dos veces a su partida → F4.
- `controller/GameplayController.java:32-36` + `service/GameplayService.createShips`: `@SendTo` a un `/topic` compartido de una vista armada para quien colocó los barcos → probablemente el rival recibe tus posiciones (a confirmar en 104) → F4. Concepto: eventos públicos (`/topic`) vs. estado privado (`/user/queue`).
- Contrato REST inconsistente: datos en la ruta (`/api/match/games/{nation}/{location}`), acciones en la URL (`/nation/{nation}`, `/emote/{id}/{emote}`), `GamePlayer` interno expuesto (`/gp/{id}`), dos prefijos para games, `Map<String,Object>` como respuesta, misma acción por REST y por WS → F4. Concepto: REST orientado a recursos, contrato tipado, no filtrar el modelo interno.
- WebSocket sin `@MessageExceptionHandler`: los errores se pierden en silencio → F4. Concepto: formato de error único también en APIs asíncronas.
- Probar contra H2 cuando producción es Postgres: los tests pueden pasar y producción fallar → F3. Concepto: paridad dev/prod (12-factor), Testcontainers.
- `static/web/js/games-mjs/player.js:14`: llama a `/api/logout` pero el backend expone `/api/players/logout` → F5. Concepto: contrato de API compartido.

## F1 · Red de seguridad

### El WebSocket de barcos nunca publicaba nada (prioridad alta)
- **Antes**: `service/GameplayService.java:47` → `GameService.java:48`; `@SendTo` en `GameplayController.java:32`. Nadie lo había notado: guardaba los barcos y fallaba en silencio.
- **Problema**: `LazyInitializationException` (`Ship.salvosImpacted`): en el hilo STOMP no hay sesión de Hibernate abierta. Como no hay `@MessageExceptionHandler`, la excepción se pierde y ningún cliente se entera.
- **Después**: F1 lo fija con un test ("se guarda pero no se publica"); se arregla en la tarea 204 (F2).
- **Por qué importa**: un test de caracterización destapa lo que el código hace de verdad, no lo que crees que hace. Además, al arreglarlo el rival recibiría el `GameDataDTO` calculado para quien colocó los barcos: arreglar un bug destapa un fallo de seguridad.
- **Concepto**: caracterizar lo observado, errores explícitos en APIs asíncronas, eventos públicos vs. estado privado.

### Mockito: matchers no son valores (prioridad media)
- **Antes**: `MatchServiceTest.java:47,65`: `Authentication authMock = any(...)` y `thenReturn(any(GamePlayer.class))`; el test no afirmaba nada.
- **Problema**: `any()` solo vale dentro de `when(...)`/`verify(...)`; fuera devuelve null y el test pasa sin probar nada.
- **Después**: `mock()` para dobles, stubs con valores reales y `verify` + asserts (3 casos).
- **Concepto**: un test que no puede fallar no protege nada.

### Probar por el contrato público, no por los internos (prioridad media)
- **Antes**: `MatchControllerIntegrationTest` firmaba el JWT con `ENV_VARIABLES.ALGORITHM`.
- **Problema**: el test dependía del secreto; al moverlo a configuración (F2) se habría roto.
- **Después**: `AuthHelper` obtiene el token llamando al login real.
- **Concepto**: acoplar tests a la interfaz pública, no a la implementación.

### Mensajes de error que mienten (prioridad media)
- `GameValidation.containsThePlayer` responde 409 "Player Already in Game" cuando el jugador NO está en la partida. Un nombre o mensaje falso cuesta horas de depuración. → F4.

### Build reproducible
- **Antes**: `./mvnw` no funcionaba (faltaba `.mvn/wrapper/`) y Lombok 1.18.22 no compilaba en JDK 21.
- **Después**: wrapper commiteado y `<lombok.version>1.18.36</lombok.version>` en `pom.xml:17`, que sobrescribe la versión que gestiona el BOM de Boot.
- **Concepto**: build reproducible; sobrescribir versiones del BOM con propiedades.

## F2 · Limpieza

### Secreto en el código y una interfaz de constantes (prioridad alta)
- **Antes**: `config/ENV_VARIABLES.java`: secreto JWT en una constante, expiración de ~700.000 años, `ALGORITHM` estático; `JWTAuthorizationFilter` duplicaba la lógica de `JWTUtil` con `new` a mano.
- **Problema**: el secreto quedó en el historial de git (comprometido para siempre); no se podía cambiar por entorno; dos implementaciones de lo mismo.
- **Después**: `application-{dev,prod}.properties` con `jwt.secret=${JWT_SECRET}` (sin valor por defecto en prod), `JWTUtil` recibe secreto y expiración por constructor (`@Value`), valida al arrancar (≥32 caracteres) y se inyecta en el filtro.
- **Por qué es mejor**: prod no arranca sin secreto (fail-fast), el secreto vive fuera del repo, hay un único punto de verdad para firmar y verificar.
- **Concepto**: Twelve-Factor (config en el entorno), fail-fast, DRY, inyección de dependencias, perfiles de Spring.

### Código muerto que parece funcionalidad (prioridad media)
- **Antes**: `SalvoController` con ~250 líneas de stubs que siempre devolvían 401/403, lógica vieja comentada y `@Autowired` sin usar; `ShipValidation` con métodos `void` que calculaban un booleano y lo descartaban, nadie lo inyectaba.
- **Problema**: ruido, superficie de ataque sin sentido, y la falsa sensación de que los barcos se validaban (no se validaban).
- **Después**: un controller de ~20 líneas; `ShipValidation` borrado (la validación real se diseña en el dominio en F4).
- **Concepto**: YAGNI; git ya guarda el código viejo, no hace falta dejarlo comentado. Borrar con evidencia (`grep`) antes de tocar.

### El límite transaccional va en el servicio (prioridad media)
- **Antes**: `GameplayService.createShips` sin transacción: en el hilo STOMP no hay sesión de Hibernate → `LazyInitializationException` y el mensaje nunca salía.
- **Después**: `@Transactional` en el método del servicio.
- **Por qué es mejor**: la sesión vive durante la operación completa sin cambiar el tipo de fetch ni activar `open-in-view` globalmente.
- **Concepto**: unidad de trabajo / límite transaccional en la capa de servicio.

### Un test intermitente se arregla buscando la causa, no repitiendo (prioridad alta)
- **Antes**: `AuthIntegrationTest.java:75-76`: el test alteraba el último carácter del token para romper la firma.
- **Problema**: la firma HMAC-SHA256 (32 bytes) en base64url deja en el último carácter solo 4 bits útiles; si el cambio cae en los 2 bits de relleno, la firma decodificada es idéntica y el token sigue válido. El test fallaba ~1 de 4 corridas.
- **Después**: se altera el primer carácter de la firma (6 bits útiles); 12 corridas seguidas en verde.
- **Concepto**: tests deterministas; un test que manipula datos codificados debe cambiar un bit que importe. Un fallo intermitente casi siempre tiene una causa concreta: caza el fallo con un bucle y captura el mensaje.

### Dos premisas de bugs resultaron falsas (prioridad media)
- Las notas sobre `Long ==` (`Game.java:82`, `GameService.java:41`) eran erróneas: los ids son `long` primitivo. La verificación con un test y con el compilador lo mostró dos veces. **Antes de arreglar, comprueba que el bug existe.**

## F3 · Upgrade

## F4 · Arquitectura backend

## F5 · Front nuevo

## F6 · Deploy
