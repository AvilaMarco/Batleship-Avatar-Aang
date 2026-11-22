# 809 · backend-dev · Nación LOTUS (Loto Blanco)
Estado: pendiente
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
