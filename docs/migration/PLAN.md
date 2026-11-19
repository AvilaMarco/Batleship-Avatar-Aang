# Plan de migración — The Last Shipbender

Lo edita solo el chat central. Cada fase se cierra con aprobación del usuario y merge de `migration/fN-*` a `main`.

## Estado de fases

| Fase | Rama | Estado | Criterio de cierre |
|---|---|---|---|
| F0 · Preparación | `migration/f0-preparacion` | en curso | Usuario aprueba plan y tareas de F1–F2 |
| F1 · Red de seguridad | `migration/f1-tests` | pendiente | `./mvnw test` verde sobre Boot 2.6 sin tocar código de producción |
| F2 · Limpieza | `migration/f2-limpieza` | pendiente | F1 verde; sin código muerto ni secretos en el código |
| F3 · Upgrade | `migration/f3-upgrade` | pendiente | Boot 4.x + Java 21 + Postgres/Flyway + Testcontainers; F1 verde; app levanta |
| F4 · Arquitectura backend | `migration/f4-arquitectura` | pendiente | Paquete `dev.racnar.shipbender`; paquetes por feature; dominio testeado sin Spring; contrato API v2 (ADR 003) implementado |
| F5 · Front nuevo | `migration/f5-frontend` | pendiente | Partida completa jugable en React + TS |
| F6 · Deploy | `migration/f6-deploy` | pendiente | Juego en el dominio vía Cloudflare Tunnel, con WebSocket |

Paralelismo permitido: F5 en paralelo con F4 una vez congelado el contrato de API; Dockerfile (F6) desde el fin de F3.

## Tareas

Detalle en `tasks/`. Terminadas se mueven a `tasks/done/`.

| # | Rol | Tarea | Fase | Estado | Depende de |
|---|---|---|---|---|---|
| 101 | backend-dev | Build reproducible: wrapper + Lombok compatible con JDK 21 | F1 | pendiente | — |
| 102 | qa | Auth: login, registro, JWT y `AuthHelper` | F1 | pendiente | 101 |
| 103 | qa | REST de games y match | F1 | pendiente | 102 |
| 104 | qa | WebSocket STOMP `/app/{id}/ships` | F1 | pendiente | 102 |
| 105 | qa | Arreglar MatchServiceTest (Mockito) + tests de GameValidation | F1 | pendiente | 101 |
| 201 | backend-dev | Borrar código muerto backend (stubs, ShipValidation, jjwt, devtools duplicado) | F2 | pendiente | F1 |
| 202 | backend-dev | Secreto/expiración JWT a config externa, perfiles dev/prod, h2-console off en prod | F2 | pendiente | 201, 102 |
| 203 | frontend-dev | Borrar código muerto front (sobras.js, delete/, TEST_DATA.js) | F2 | pendiente | — |

Orden de ejecución F1: 101 → (102 ∥ 105) → (103 ∥ 104). F2: (201 ∥ 203) → 202.

Línea base (F0): `./mvnw test` no ejecuta (falta `.mvn/wrapper/`; Lombok 1.18.22 no compila en JDK 21). Con Lombok 1.18.36: 3 tests triviales en verde.

## Decisiones

- [ADR 001 — Decisiones base de la migración](../adr/001-decisiones-base.md)
- [ADR 002 — Paquete base, Testcontainers, estilo de arquitectura y contrato v2](../adr/002-paquete-tests-arquitectura-contrato.md)

Orden previsto al inicio de F4: renombre de paquete → ADR 003 contrato v2 (arquitecto) → reorganización por feature → implementación v2 → qa actualiza tests del contrato viejo.

## Pendiente del usuario

- Renombrar `master` → `main` en GitHub (Settings → Branches) y luego `git push -u origin main`. Local ya está en `main`.
