# 809 · backend-dev · Nación LOTUS (Loto Blanco)
Estado: hecha
Fase: F8
Depende de: —
Paralelizable: sí (solo backend)
Origen: tarea 808 punto 6 ("`LOTUS` en `NationType`, selector y backend").
Riesgo: alto (migración Flyway sobre datos existentes) → tarea de `reviewer` antes del merge.

Objetivo: añadir `LOTUS` a `NationType` y permitirla en BD, sin romper datos existentes.

Archivos: `shared/NationType.java`, nueva migración `src/main/resources/db/migration/V4__nation_lotus.sql` (NO editar V1-V3): reemplazar los `check` de `game.nation` y `player.nation` para incluir `'LOTUS'` (reconstruir el constraint; comprobar su nombre real en Postgres), y los tests (TDD; Testcontainers): registrar jugador y crear partida con `LOTUS` → 201; valor inválido sigue → 400; datos previos intactos tras migrar. Revisar `Validator`/DTOs/Swagger por listas fijas de naciones. No tocar el front (tarea 808).

Criterios de aceptación:
- `./mvnw test` → exit 0 (Docker corriendo), con tests nuevos de LOTUS y de la migración.
- Migración expand-only (solo ensancha el constraint), así el rollback de imagen sigue arrancando.
- Commit `feat(backend): nación LOTUS (tarea 809)`; terminar el mensaje con la línea Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado
Estado: hecha. `LOTUS` en `NationType`; `V4__nation_lotus.sql` (drop/add de `game_nation_check` y `player_nation_check`, expand-only). Tests nuevos en `NationLotusIntegrationTest` (PATCH /me y POST /games con LOTUS, PLUTO -> 400, definición del check). `./mvnw test` exit 0, 116 tests. Sin listas fijas de naciones en DTOs/Swagger (enum). No se probó migrar sobre una BD con datos reales; solo amplía el check, así que no puede invalidar filas.

### Revisión (reviewer, solo lectura)
Aprobado, sin bloqueantes. V4 es expand-only; los nombres `game_nation_check`/`player_nation_check` coinciden con los que Postgres da a los checks inline de V1 (V2/V3 no los tocan); drop+add en la misma migración transaccional; sin listas fijas de naciones en backend ni en el ADR 004.
MENOR 1: si ya existe una fila con `LOTUS`, la imagen anterior falla al leerla (enum sin ese valor). Antes de un rollback: `update player set nation = null where nation = 'LOTUS'` y lo mismo en `game`. Anotar en el runbook.
MENOR 2: el test de migración solo comprueba el texto del check; no prueba que una fila antigua sobreviva (aceptado: ensanchar no puede invalidar filas).
