package com.yushan.content_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }

    @Bean
    DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName(postgres.getDriverClassName())
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .build();
    }

    @Bean
    GenericContainer<?> redisContainer() {
        return redis;
    }
}
