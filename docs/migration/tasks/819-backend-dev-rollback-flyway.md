# 819 · backend-dev · Probar que una imagen anterior arranca sobre un esquema con V4
Estado: hecha
Fase: F8
Depende de: 809
Paralelizable: sí (solo backend)
Origen: revisión 809/622: "Flyway ignora V4 al volver a una versión anterior: dependencia no verificada"; usuario: "resuelve el resto".
Riesgo: alto (migraciones/rollback) → reviewer si el resultado cambia configuración.

Objetivo: demostrar con un test (Testcontainers Postgres) que con la configuración REAL de la app, una BD ya migrada hasta V4 no hace fallar `flyway validate/migrate` cuando la imagen anterior solo conoce V1-V3 (simúlalo con una ubicación de archivos con copias de V1-V3 y la misma configuración de Flyway que usa Spring Boot por defecto, incluidos los `ignoreMigrationPatterns`). Si falla (migración "applied but not resolved"), la solución es fijar `spring.flyway.ignore-migration-patterns=*:future` (o equivalente) en `application.properties`, con el test como prueba, y documentar en `docs/deploy/runbook.md` (sección rollback) el resultado.

Criterios: `./mvnw test` exit 0 (Docker corriendo); el test falla antes de la config si hace falta (rojo) y pasa después; commit `test: rollback de imagen con esquema V4 (tarea 819)` (+ `fix:` si cambia la config), línea final Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>.

## Resultado
Test `FlywayRollbackTest` (Testcontainers): migra a V4 y valida/migra con solo V1-V3; pasa a la primera (no hubo rojo) porque el default de Flyway es `*:future` y Spring Boot no lo cambia. Sin cambio de configuración; runbook sección 4 actualizada. `./mvnw -Dtest=FlywayRollbackTest test` exit 0.
