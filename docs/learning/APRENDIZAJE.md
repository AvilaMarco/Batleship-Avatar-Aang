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

### Migrar por saltos pequeños, con una red de tests entre cada uno (prioridad alta)
- **Antes**: Spring Boot 2.6.6 sin soporte, Java 17, H2.
- **Después**: 2.7 → 3.5 → Java 21 → 4.2, con los tests de F1 en verde tras cada salto (una tarea y un commit por salto).
- **Por qué es mejor**: cuando algo se rompe sabes exactamente qué salto lo causó. Con un solo salto 2.6 → 4.2 habría una mezcla imposible de separar (jakarta, Hibernate 7, Jackson 3...). Java 21 va después de Boot 3.5 porque Boot 2.7 no leía clases de Java 21: el **orden** de los saltos importa.
- **Concepto**: cambios incrementales, red de seguridad de tests, una sola variable por paso.

### Una receta automática es un PR ajeno (prioridad media)
- OpenRewrite dejó Boot 4.0 en lugar de 4.2, reindentó el `pom.xml`, añadió dependencias redundantes y fijó Lombok a mano contra la gestión de versiones de Boot. Se revisó el diff y se quedó solo lo que cambia comportamiento. **Revisa siempre lo que genera una herramienta como si lo hubiera escrito otra persona.**

### Deny-by-default: lo que antes fallaba en silencio ahora se ve (prioridad alta)
- **Antes**: `WebSecurityConfig`: `anyRequest().permitAll()` y matchers que no eran rutas HTTP (`/topic/**`, `/app/**`) ni existían (`/users/admin`).
- **Después**: lista cerrada de rutas públicas y `anyRequest().denyAll()`. Los destinos STOMP se protegen en su capa (interceptores de mensajería), no en la HTTP.
- **Por qué es mejor**: el error pasa de "expuesto" a "403 visible". Sacó a la luz que el front llamaba a `/api/logout` (no existe; la real es `/api/players/logout`): antes daba 404 y nadie lo notó.
- **Concepto**: mínimo privilegio, deny-by-default, separar capas de seguridad (HTTP vs. mensajería).

### El esquema vive en el repo: migraciones con Flyway (prioridad alta)
- **Antes**: `ddl-auto=create-drop` sobre H2 en memoria: el esquema lo inventaba Hibernate en cada arranque y los tests no probaban el motor de producción.
- **Después**: `V1__init.sql` versionado, `ddl-auto=validate` (Hibernate solo comprueba), Postgres real en los tests con Testcontainers.
- **Por qué es mejor**: paridad dev/test/prod; el esquema es código revisable y se puede evolucionar sin perder datos. H2 ocultaba errores reales (dialecto distinto: el DDL de H2 rompió 20 tests sobre Postgres).
- **Concepto**: *schema as code* (database migrations), paridad de entornos, aislamiento de estado entre tests (un contenedor por contexto para que los datos de `initData` no se dupliquen).

### Probar la mutación que de verdad cambia el comportamiento (prioridad media)
- La tarea pedía quitar `setHeartbeatValue(10000,10000)` para ver fallar el test; no falló porque ese valor coincide con el default de Spring cuando hay scheduler. Un test solo protege algo si falla ante el defecto que dice proteger.

### Hallazgo confirmado en vivo: el rival recibe tus barcos (prioridad alta, se arregla en F4)
- **Antes**: `GameplayController` envía con `@SendTo("/topic/gameplay/{id}/ships")` el `GameDataDTO` calculado para quien colocó los barcos.
- **Problema**: el topic lo reciben todos los suscriptores. En la prueba en el navegador, A recibió las posiciones exactas de los 5 barcos de B. En un juego de batalla naval es hacer trampa por diseño. Estaba oculto porque el mensaje ni se publicaba (`LazyInitializationException`, F2): al arreglar un bug apareció otro.
- **Después (F4)**: eventos públicos en `/topic` (sin posiciones) y estado privado por usuario en `/user/queue`.
- **Concepto**: información privada vs. pública en el canal; lo que el servidor *puede* enviar no es lo que *debe* enviar a cada cliente.

### Dos fallos del front original, para F5
- `game.js:99`: al abrir el creador su partida sin rival, `CLIENT` es `undefined` y revienta antes de conectar el socket. `update_html.js:93`: `createShipGrid` revienta al recibir los barcos del rival. Ambos ya existían; los tapaba el estado global y la ausencia de tipos. Con TypeScript, `find()` devuelve `T | undefined` y el compilador te obliga a manejar ese caso.

## F4 · Arquitectura backend

### Reglas del juego en el dominio, no en servicios ni controllers (prioridad alta)
- **Antes**: `GameService.java:39-46` mutaba entidades al armar una respuesta de lectura (`emptyShips()`); `GameplayService.java:44-47` copiaba los barcos del cliente sin validar (`NONAME`, celdas inventadas); `ShipValidation` existía pero nadie la llamaba; el estado de la partida se calculaba con índices 0/1 de una lista sin orden garantizado (`Game.java:104-112`); y la entidad `Game` recibía un DTO builder (`Game.java:48-73`).
- **Problema**: las reglas estaban repartidas o ausentes; no se podían probar sin Spring ni base de datos; una *lectura* podía destruir datos; el dominio dependía de la capa web.
- **Después**: `Fleet` valida en su constructor (una flota que existe ya es válida: *parse, don't validate*); `Game.join/placeShips/fire/status()` concentran las reglas (raíz del agregado); `GameView.of` es una proyección que solo lee. 45 tests de dominio corren en milisegundos, sin Docker.
- **Por qué es mejor**: la regla vive en un solo sitio, se prueba aislada y el servicio solo orquesta (cargar, llamar al dominio, guardar).
- **Concepto**: modelo de dominio rico vs. anémico, aggregate root, SRP, regla de dependencias (el dominio no conoce la web), hacer ilegales los estados inválidos.

### Eventos de dominio publicados después del commit (prioridad alta)
- **Antes**: `@SendTo` en el controller empujaba el mensaje a STOMP durante la petición: un rollback posterior habría dejado un evento fantasma ya enviado, y el mensaje llevaba datos del emisor.
- **Después**: el servicio publica un evento (`PlayerJoined`, `ShipsPlaced`, `SalvoFired`...) y un único `@TransactionalEventListener(AFTER_COMMIT)` lo envía a `/topic/games/{id}`. Los eventos públicos nunca llevan celdas.
- **Por qué es mejor**: el servicio no conoce STOMP; solo se anuncia lo que ya está confirmado en la base; la privacidad es una propiedad del evento.
- **Concepto**: domain events, patrón Observer, adaptador de salida único, separar eventos públicos (`/topic`) de estado privado (`/user/queue`).

### Contrato de API consistente (prioridad alta)
- **Antes**: datos en la ruta (`/games/{nation}/{location}`), verbos (`/nation/{n}`), `GamePlayer` expuesto, respuestas `Map<String,Object>`, errores con formas distintas, 404 por login fallido, el error "Player Already in Game" para un no participante.
- **Después**: recursos y verbos HTTP (`POST /api/games`, `PATCH /api/players/me`), vistas relativas al que pregunta (`me`/`opponent`), un solo formato de error `ProblemDetail` con `code` estable (REST y STOMP), 401/403/409 con significado. Documentado en un ADR antes de implementar.
- **Concepto**: REST orientado a recursos, contrato antes que código, RFC 9457, errores como parte de la API, mínima exposición (un ranking sin emails).

### Test primero: el rojo es la especificación (prioridad alta)
- qa escribió los tests del contrato v2 *antes* de que backend-dev implementara (31 rojos, luego 14, luego 0; después 7 a 0 y 10 a 0). Los tests "bug conocido" de F1-F3 pasaron a afirmar el comportamiento correcto. Un test que fijaba la fuga de barcos pasó a prohibirla.
- **Por qué es mejor**: quien implementa tiene un criterio objetivo ("ponlo en verde") y quien escribe el test no es quien escribe el código. **Coste**: la rama queda en rojo entre pares de tareas.
- **Concepto**: TDD (rojo, verde, refactor), tests como especificación ejecutable.

### Abstracciones que no pagaban (prioridad media)
- 8 interfaces `I*Service` de una sola implementación, `ModelMapper`, una jerarquía de excepciones con `HttpStatus` dentro (el dominio dependía de la web), `ENV_VARIABLES` como interfaz de constantes. Se borraron. Quedó una `ShipbenderException` con un `ErrorCode`.
- **Concepto**: YAGNI; abstraer cuando hay una segunda implementación, no antes. Las abstracciones valen donde protegen algo que cambia (aquí, las reglas del juego).

### Detalles que costaban caro (prioridad media)
- `PlayerService.java:104` dividía enteros: el `win_rate` solo daba 0 o 100. Ahora `won * 100.0 / total` (`PlayerStats`).
- `Player.roles` se guardaba como ordinal: reordenar el enum habría cambiado los roles de todos. Ahora `EnumType.STRING` (V3).
- Registro: un `exists` antes del `insert` deja pasar la carrera (500). La unicidad la decide la base (`unique`) y se traduce a 409.
- `Salvo` con `@Data` y relación bidireccional: `equals/hashCode/toString` ciclan. En entidades JPA, `@Getter/@Setter`.
- Seguridad: con `/api/**` abierto a PLAYER, una ruta inexistente daba 404 (revela qué existe); deny-by-default real lista las rutas permitidas.
- El token con firma manipulada pasó de excepción sin respuesta a `401 UNAUTHENTICATED`: la autenticación solo establece identidad; la autorización decide después.

## F5 · Front nuevo

### De estado global y DOM a funciones puras tipadas (prioridad alta)
- **Antes**: `game.js:37,99` y `update_html.js:93`: variables globales mutables (`HOST`, `CLIENT`, `GAME`, `HEADER.id` reasignado entre suscripciones), lógica de barcos mezclada con el DOM. Si el creador abría la partida sin rival, `CLIENT` era `undefined` y la pantalla reventaba antes de conectar el socket.
- **Problema**: cualquier orden de eventos distinto al esperado rompía la pantalla, y nada obligaba a pensar en el caso "no hay rival todavía".
- **Después**: `domain/` en TypeScript puro (`phase(view)`, `outcome(view)`, `enemyBoard`, `toggleTarget`, `shipCells`): funciones sin React ni red, 31 tests. La fase de la pantalla se **deriva** de los datos del servidor; `opponent` es `null` en el tipo y el compilador obliga a manejarlo.
- **Por qué es mejor**: el estado no se guarda dos veces (no puede desincronizarse), la regla se prueba sin montar nada, y el tipo `T | null` convierte un crash en un caso explícito.
- **Concepto**: funciones puras, estado derivado vs. almacenado, tipos que hacen explícitos los casos límite, separar lógica de presentación.

### Una sola fuente de verdad: volver a pedir, no reconstruir (prioridad alta)
- **Decisión** (ADR 006): ante cualquier evento STOMP salvo emote, la pantalla hace de nuevo `GET /api/games/{id}`. No se aplican los eventos a un estado local con un reducer.
- **Por qué**: un reducer duplicaría en TypeScript las reglas del servidor (impactos, turnos) y divergiría. Los eventos son solo una señal de "algo cambió", y el servidor (que publica tras el commit) es la verdad.
- **Concepto**: single source of truth, eventos como notificación y no como transporte de estado (*event notification* vs. *event-carried state transfer*).

### Un único punto de acceso a un secreto (prioridad alta)
- **Antes**: el token JWT en `localStorage` leído desde varios archivos, y un `HEADER` global.
- **Después**: `api/auth.ts` es el único módulo que toca web storage (grep lo comprueba), `ApiError` solo lleva `status/code/detail` (el token no puede acabar en un mensaje), y el token nunca va en la URL ni en logs.
- **Matiz de seguridad (revisión independiente)**: `sessionStorage` tampoco evita un XSS (solo acorta la vida del token a la pestaña); la defensa real es no renderizar HTML crudo y una **Content-Security-Policy**, que se añadió en la tarea 514. Se eligió `sessionStorage` en dev (jugar A contra B en dos pestañas) y `localStorage` en prod.
- **Concepto**: encapsular el secreto, defensa en profundidad, no confiar en el tipo de almacenamiento sino en la ausencia de XSS.

### Lista blanca en la frontera de confianza (prioridad media)
- El backend acepta cualquier texto ≤32 como emote; armar `/assets/emotes/${texto}.png` con lo que envía el rival habría dejado cargar rutas arbitrarias. `isKnownEmote` filtra contra una lista cerrada y descarta el resto. El cliente no confía en datos del otro jugador.
- **Concepto**: allowlist vs. denylist, validar en la frontera, nunca construir rutas con datos externos.

### Probar lo que los mocks no ven (prioridad media)
- Los tests de pantalla usan `api` y `stomp` simulados: no prueban el WebSocket real. La prueba manual en el navegador encontró (y cerró) lo que los tests no cubrían: sincronización en vivo a través del proxy de Vite, la CSP con el front servido por el jar, el token inválido. El E2E de Playwright automatiza después el guion completo (dos jugadores, 4 turnos, emote) con localizadores por rol y sin esperas fijas.
- **Concepto**: pirámide de tests (unitarios rápidos, pocos E2E), sincronizar por estado observable y no por tiempo, mocks vs. integración real.

### Revisión independiente de lo crítico (prioridad media)
- El rol `reviewer` (solo lectura) revisó la tarea de JWT y rutas públicas y encontró lo que quien implementó no vio: el token caducado en reconexión STOMP (bucle de reintentos), la falta de CSP, `/favicon.svg` sin permitir y contraseñas sin tope. El implementador no puede revisar su propio trabajo con la misma mirada.
- **Concepto**: revisión por pares para riesgo alto, defensa en profundidad.

### Detalles de integración (prioridad baja)
- Un perfil de Maven opcional (`frontend`) para lo que necesita red (descargar Node): `./mvnw test` sigue hermético. Un test de "rutas no listadas" que solo comprobaba `!= 403` pasaba en verde sin probar nada porque un anónimo recibe 401: había que descartar ambos.

## F6 · Deploy

### Probar la operación destructiva en un entorno aislado (prioridad alta)
- **Antes** (en el diseño): `restore.sh` asumía que todo saldría bien; con `set -e` un `pg_restore` fallido dejaba `app` detenida y la base a medias, sin aviso. Además la retención podía borrar el propio dump de seguridad, y `docker exec` se comía la entrada estándar, así que la confirmación "sí" nunca se leía.
- **Después**: validación previa (`pg_restore -l`) antes de parar nada, dump de seguridad con retención casi infinita, restauración en **una sola transacción** (`--single-transaction --exit-on-error`) y un `trap` que arranca `app` siempre, nombrando el dump de seguridad. Se probó un fallo real a mitad: la transacción se revirtió y el recuento no cambió.
- **Por qué es mejor**: una restauración o funciona entera o no cambia nada. Los cinco fallos aparecieron solo **al ejecutarla**, no al leerla.
- **Concepto**: operaciones destructivas atómicas, copia de seguridad previa, probar el camino de fallo y no solo el feliz.

### Seguro por defecto: el perfil `dev` falla abierto (prioridad alta)
- `application.properties:23` (`spring.profiles.default=dev`): si en producción falta el perfil, arrancarían el seeder (admin con contraseña `123`) y el secreto de dev. Se mitiga fijando `SPRING_PROFILES_ACTIVE=prod` en la imagen y exigiendo el secreto con `${VAR:?}` en el compose. Principio: *secure by default*: "sin perfil = no arranca" es mejor que "sin perfil = dev".

### La frontera de confianza detrás de un proxy (prioridad alta)
- Detrás de cloudflared el TLS termina fuera: Tomcat ve `http` y la comprobación de mismo origen del WebSocket fallaba (403). La solución es `server.forward-headers-strategy=native` (solo confía en `X-Forwarded-*` de IPs privadas), sin declarar `allowedOrigins`. Detalle que solo se ve probando: con `X-Forwarded-Proto: https` Tomcat fija el puerto 443, así que el `Origin` debe ir **sin puerto**, como el que envía cloudflared.
- La CSP pasó de `connect-src 'self' ws: wss:` (que permitía sacar el token por WebSocket a cualquier servidor) a `connect-src 'self'`. Principio: mínimo privilegio, hacer explícita la frontera de confianza.

### Aislar lo que no es red de Internet y fijar versiones (prioridad media)
- `internal: true` en la red de `db` y `backup` impide su salida a Internet, pero solo aísla a quienes no están además en otra red (`app` sí tiene salida). Ningún servicio publica puertos: el único camino de entrada es el túnel saliente de cloudflared. Las imágenes van por etiqueta de versión (`cloudflared:2026.11.1`, no `latest`): un `pull` no cambia el túnel sin aviso.
- **Concepto**: superficie de ataque mínima, red por capas, infraestructura reproducible.

### Docker: capas, caché y fin de línea (prioridad media)
- Copiar los manifiestos antes del código y usar *cache mounts* hace que un cambio en Java no reinstale Node ni baje dependencias de Maven. Un script con CRLF falla dentro de Linux (`sh: no such file`), por eso `.gitattributes` fuerza LF en `mvnw` y `*.sh`. Una imagen multi-etapa deja fuera de la imagen final el JDK, Node y las fuentes.
- **Concepto**: orden de capas, builds multi-etapa, mínimo en producción.

### Un runbook solo es fiable si cada comando se ejecutó (prioridad media)
- Al ejecutar cada comando contra el proyecto aislado aparecieron hallazgos que ninguna lectura habría dado: en Git Bash una ruta `/tmp/...` se monta en la VM de Docker y el dump no aparece en el host; `docker compose start` devuelve 0 aunque el servicio no exista, así que el código de salida no basta para decir "app arrancada".
- **Concepto**: documentación verificable, no confiar en el código de salida de un comando, probar en entorno aislado.


## F7 · Endurecimiento (parte 1: contraseña)

### Validar con la misma unidad que usa el consumidor final (prioridad alta)
- **Antes**: `RegisterRequest`: `@NotEmpty` y, tras F5, `@Size(max = 72)`. Parecía suficiente.
- **Problema**: `@Size` cuenta **caracteres**, pero bcrypt corta en **bytes**. 40 caracteres "é" son 80 bytes: pasaban la validación y el encoder lanzaba `IllegalArgumentException` (500). Además, sin mínimo, una contraseña de 1 carácter era válida.
- **Después**: `@Size(min = 8, max = 72)` y una restricción propia `@MaxUtf8Bytes(72)` sobre el campo (validador que cuenta bytes UTF-8). Un primer intento con `@AssertTrue` dejaba el error en una clave artificial (`passwordWithin72Bytes`, "must be true"), ilegible para el front; la restricción sobre el campo lo deja en `errors.password` con un mensaje claro.
- **Por qué es mejor**: la validación vive en la frontera (el DTO), usa la unidad que importa (bytes) y devuelve 400 en vez de un 500. El login no cambia, para no romper cuentas existentes.
- **Concepto**: validar en el *trust boundary*, restricciones a medida de Bean Validation (`ConstraintValidator`), contrato de errores por campo.

### Probar el comportamiento real, no el simulado (prioridad media)
- La tarea del front dio por hecho que el error llegaba como `errors.password` porque su test lo simulaba; el test de integración del backend mostró que llegaba como `passwordWithin72Bytes`. Un test que simula la respuesta del servidor no prueba que el servidor responda así.
- También: el límite de bcrypt se **probó con el encoder real** antes de añadir código, y el test se validó con una **mutación** (quitar la validación → 500).
- **Concepto**: pruebas de integración vs. dobles, mutación para comprobar que un test detecta el defecto.


## F7 · Deploy en el servidor real

### Reproducir el fallo antes de arreglarlo (prioridad alta)
- **Antes**: `deploy.sh:8` usaba `trap '' HUP` pensando que así el despliegue sobrevivía a un corte de SSH.
- **Problema**: al caer la sesión se cierran las tuberías de salida y el siguiente `echo` mata el script con **SIGPIPE** (con `set -e`, un `echo` fallido mata el script). Si caía durante la espera de salud (hasta 180 s) con una versión mala, el script moría **antes del rollback**: `app` en bucle de caídas y `.current_tag` apuntando a la versión mala. Justo el caso que el rollback debe cubrir.
- **Después**: `detach()` redirige stdout/stderr a `.deploy.log` (con `tee`) antes de operar y `trap '' HUP PIPE`. La tarea exigió **reproducir el fallo contra el código viejo** (`up ... | head -n 1`) y luego demostrar el arreglo con el mismo caso.
- **Concepto**: un `trap` de una señal no protege de otra; desacoplar la salida de la sesión; probar el camino de fallo, no el feliz; reproducir antes de arreglar.

### Compilar una vez, fuera del servidor: artefacto inmutable (prioridad alta)
- El servidor tiene poca RAM libre y **no sale a Internet salvo por el túnel**: no puede compilar ni hacer `docker pull`. Se compila en la PC de desarrollo y se envía **la imagen** (`docker save | gzip | ssh | docker load`, 131 MB medidos; incluye también las imágenes base la primera vez). Lo que se probó es lo que se ejecuta, y el servidor no necesita Maven, Node ni Internet.
- Se midió en lugar de estimar: una estimación externa decía 150-200 MB comprimidos y el dato real fue 131 MB; el pico de RAM está en la **build** (~1,2 GB), no en funcionamiento (~350 MB).
- **Concepto**: artefacto inmutable ("build once, run anywhere"), separar build y runtime, decidir con datos medidos.

### Una lista blanca protege de la inyección, no del contenido (prioridad alta)
- `ssh-entry` solo acepta cinco comandos con regex estricta y sin `eval`/`sh -c` (probado con 20 entradas hostiles y un archivo canario). Pero la clave restringida **equivale a root** del servidor: `docker load` acepta cualquier imagen y `infra` puede sustituir el compose. La barrera real es la clave: dedicada, con `from="IP"`, usuario sin sudo. Se documenta como riesgo aceptado.
- **Concepto**: mínimo privilegio, distinguir validar el *comando* de validar el *contenido*, `restrict` en `authorized_keys`.

### El rollback cubre la imagen, no la configuración (prioridad media)
- Con un `JWT_SECRET` corto en `.env`, la versión anterior también falla (usa el mismo `.env`). El rollback automático protege contra imágenes malas, no contra una configuración mala: queda documentado.
- Y: `profiles` de Compose no impiden que se **interpole** `${VAR:?}` de los servicios inactivos (`TUNNEL_TOKEN`), por eso es `${TUNNEL_TOKEN:-}`; un healthcheck sin `start_period` falló 1 de 4 arranques en frío porque `pg_isready` tarda ~20 s durante `initdb`; y `mem_limit` sin `memswap_limit` deja usar el swap del servidor.
- **Concepto**: conocer los límites de cada mecanismo de seguridad, configuración vs. artefacto, comprobar el supuesto antes de aplicarlo.


## F8 · Mapa, diseño conectado y pantallas

### Una migración que ensancha es reversible; una que estrecha, no (prioridad alta)
- **Antes**: el `check` de `nation` en V1 solo admitía 4 naciones; añadir `LOTUS` en el enum no bastaba.
- **Problema**: editar V1 rompería el historial de Flyway; y si ya hay filas con `LOTUS`, volver a la imagen anterior (que no conoce ese valor) falla al leerlas.
- **Después**: V4 hace `drop constraint` + `add constraint` con la lista ampliada (*expand-only*), sin tocar V1-V3. Para el rollback se documenta poner `nation = null` donde valga `LOTUS`, o restaurar el backup. La tarea 819 comprueba con un test que Flyway no se queja de una migración "futura" al volver atrás.
- **Concepto**: migraciones expand/contract, nunca editar una migración aplicada, el rollback de imagen no revierte la base de datos.

### Dividir una tarea cuando cambia su riesgo (prioridad alta)
- La tarea 808 se escribió como "riesgo bajo" pero uno de sus siete puntos (`LOTUS`) necesitaba una migración de base de datos, riesgo alto según CLAUDE.md. Se separó en 809 (backend, con revisión) y 808 (front), y los tests del front (810) se escribieron antes de implementar.
- **Concepto**: el riesgo de una tarea es el de su punto más arriesgado; test primero fija el contrato (y los contratos que el rol de tests inventa quedan escritos en su informe).

### Un test que choca con el cambio no lo edita quien implementa (prioridad media)
- Dos veces (811, 817) el desarrollador encontró un test existente en conflicto con el diseño (un heading que ya no existe; un `getAllByRole` que lanza si hay 0 resultados). En lugar de editarlo, creó una tarea para `qa`. Así quien implementa no puede "ponerse verde" debilitando la prueba.
- **Concepto**: separación de roles como control; `getAllBy*` lanza con 0 coincidencias, `queryAllBy*` devuelve vacío.

### Los selectores de e2e son un contrato con la interfaz (prioridad media)
- El e2e buscaba `: empty` y la UI ya decía "libre" desde un commit anterior: los tests unitarios pasaban, el e2e no. Se detectó al correrlo, no antes. Y un clic justo después del login se perdía porque el componente se remonta: se arregló esperando el efecto visible ("Actualizar") y no con un `sleep`.
- **Concepto**: esperar el resultado observable, no el tiempo; correr el e2e al cerrar cada fase.

### Verificar una "mejora" contra la decisión ya registrada (prioridad media)
- El PLAN decía que el refresco cada 15 s se descartaba, pero las tareas 804/805 seguían como `pendiente` y se ejecutaron tal cual. Resultado inofensivo, pero contradecía una decisión. Lección: al retomar trabajo, leer primero el PLAN y no solo los archivos de tarea; si no coinciden, preguntar o dejarlo anotado.
- **Concepto**: una sola fuente de verdad del estado; las tareas obsoletas se marcan como descartadas en su propio archivo.

### Medir en la app real, no solo mirar capturas (prioridad baja)
- Las capturas del navegador integrado salen en baja resolución; el desborde del ranking en 390 px se encontró midiendo `scrollWidth` frente a `innerWidth` en la página real, y el arreglo se comprobó con la misma medida. Un 403 aparente resultó ser un dato del seeder (usuario sin rol `PLAYER`), no un bug.
- **Concepto**: instrumentar en vez de adivinar; distinguir un fallo de la app de uno de los datos de prueba.
