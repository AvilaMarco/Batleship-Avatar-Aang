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
- `models/Game.java:82`, `controller/SalvoController.java:96`: `Long == Long` (falla con ids > 127 por el caché de Integer/Long) → F4. Concepto: identidad vs. igualdad.
- `security/JWTAuthorizationFilter.java:47`: atrapa excepciones de jjwt pero verifica con auth0, y duplica `JWTUtil` → F2. Concepto: DRY, una librería por responsabilidad.
- `ENV_VARIABLES.java`: interfaz de constantes implementada por 3 clases → F2. Concepto: antipatrón *Constant Interface*.
- `GameValidation.containsThePlayer` lanza `PlayerAlreadyInGameException` cuando el jugador NO está; `PlayerService.getPlayerAuthenticated` ignora su parámetro y lee `SecurityContextHolder` → F4. Concepto: nombres honestos, dependencias explícitas.
- `service/MatchService.java:58`: validación "ya está en la partida" comentada → un jugador se puede unir dos veces a su partida → F4.
- `static/web/js/games-mjs/player.js:14`: llama a `/api/logout` pero el backend expone `/api/players/logout` → F5. Concepto: contrato de API compartido.

## F1 · Red de seguridad

## F2 · Limpieza

## F3 · Upgrade

## F4 · Arquitectura backend

## F5 · Front nuevo

## F6 · Deploy
