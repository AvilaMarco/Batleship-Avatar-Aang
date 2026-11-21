package dev.racnar.shipbender;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // un contenedor por contexto: initData siembra datos y con Flyway un contenedor compartido los duplicaría
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres () {
        return new PostgreSQLContainer("postgres:17-alpine");
    }
}
