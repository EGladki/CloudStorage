package com.gladkiei.cloudstorage;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
//@PropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("testPostgres")
            .withPassword("testPostgres");

    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>("redis:8.2.3")
            .withExposedPorts(6379);

    @Container
    protected static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withExposedPorts(9000, 9001)
            .withUserName("testMinio")
            .withPassword("testMinio");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("minio.endpoint", minio::getS3URL);
    }

}



