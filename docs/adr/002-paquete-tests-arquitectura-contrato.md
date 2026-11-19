# ADR 002 — Paquete base, Testcontainers, estilo de arquitectura y contrato v2

Estado: aceptada · 2026-11-19

## Decisiones
1. **Paquete base `dev.racnar.shipbender`**, `groupId` `dev.racnar` (dominio propio `racnar.dev` invertido). Se hace al inicio de F4, en una tarea solo de renombre (sin cambios de lógica), antes de reorganizar por feature.
2. **Testcontainers con Postgres** para tests de integración desde F3 (`@ServiceConnection`). H2 se elimina cuando ya no lo use ningún test. Los tests unitarios del dominio no usan BD.
3. **Arquitectura: monolito modular por feature con dominio rico** ("Clean Architecture pragmática"). Se cumple la regla de dependencias para las reglas del juego (Java puro, sin Spring). Se aceptan conscientemente: anotaciones JPA en las entidades y uso directo de repositorios Spring Data (sin puertos/adaptadores).
4. **Contrato API v2** (REST + STOMP) diseñado por el arquitecto al inicio de F4 en `docs/adr/003-contrato-api-v2.md` (REST estilo OpenAPI, STOMP estilo AsyncAPI). Lineamientos: recursos en la URL y datos en el body; no exponer `GamePlayer`; DTOs tipados; una sola vía por acción (REST o WS); eventos públicos en `/topic`, estado privado y errores en `/user/queue`; `@Payload` en `@MessageMapping`.

## Alternativas descartadas
- Hexagonal completo (modelo de dominio + entidad JPA + mappers + puertos): duplica código para proteger de cambios de BD/framework que no se prevén.
- Mantener `/api/v1` en paralelo a v2: nadie más consume la API y el front se reescribe.

## Consecuencias
- El front viejo deja de funcionar durante F4 hasta que F5 esté lista.
- qa actualiza en F4 los tests de caracterización que fijaban el contrato viejo (cambio de comportamiento intencional).
- Los tests de integración requieren Docker corriendo.
