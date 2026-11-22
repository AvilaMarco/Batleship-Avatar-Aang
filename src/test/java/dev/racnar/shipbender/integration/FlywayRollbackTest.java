package dev.racnar.shipbender.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Rollback de imagen: una BD migrada hasta V4 debe arrancar con una imagen que solo conoce V1-V3. */
@Testcontainers
class FlywayRollbackTest {

    @Test
    void imagenAnteriorArrancaSobreEsquemaConV4 (@TempDir Path dir) throws Exception {
        try (var pg = new PostgreSQLContainer("postgres:17-alpine")) {
            pg.start();
            // Spring Boot no fija ignoreMigrationPatterns: se usa el default de Flyway, igual que aquí
            Flyway.configure().dataSource(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())
                    .locations("classpath:db/migration").load().migrate();

            for (var v : new String[]{"V1__init.sql", "V2__player_email_unique.sql", "V3__limpieza_f4.sql"}) {
                try (var in = getClass().getResourceAsStream("/db/migration/" + v)) {
                    Files.copy(in, dir.resolve(v));
                }
            }
            var anterior = Flyway.configure().dataSource(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())
                    .locations("filesystem:" + dir).load();

            anterior.validate();
            assertThat(anterior.migrate().migrationsExecuted).isZero();
        }
    }
}
