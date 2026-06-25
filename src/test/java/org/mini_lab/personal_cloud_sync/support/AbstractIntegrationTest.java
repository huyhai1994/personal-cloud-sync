package org.mini_lab.personal_cloud_sync.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class AbstractIntegrationTest {
    private static final MySQLContainer mysqldb;

    static {
        mysqldb = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("personal_sync_db")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);

        mysqldb.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqldb::getJdbcUrl);
        registry.add("spring.datasource.username", mysqldb::getUsername);
        registry.add("spring.datasource.password", mysqldb::getPassword);
    }
}

