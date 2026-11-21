package com.codeblockacademy.shipbender;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // static: los contextos distintos (p. ej. RANDOM_PORT) comparten un solo contenedor por corrida
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres () {
        return POSTGRES;
    }

    // temporal: lo borra la tarea 310 (Flyway)
    @Bean
    DynamicPropertyRegistrar ddl () {
        return registry -> {
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
            // application.properties fija H2Dialect; hasta la 310 se pisa para que Hibernate genere DDL de Postgres
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        };
    }
}
