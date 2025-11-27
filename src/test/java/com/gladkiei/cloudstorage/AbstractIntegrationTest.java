package com.gladkiei.cloudstorage;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
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

//    @BeforeAll
//    static void setUpMinio() {
//        minio.start();
//        MinioClient.builder()
//                .endpoint(minio.getS3URL())
//                .credentials("minioadmin", "minioadmin")
//                .build();
//    }


    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("minio.endpoint", minio::getS3URL);


    }

}



