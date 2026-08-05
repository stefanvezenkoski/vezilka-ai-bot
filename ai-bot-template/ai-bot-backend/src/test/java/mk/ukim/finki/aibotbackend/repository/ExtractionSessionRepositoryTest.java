package mk.ukim.finki.aibotbackend.repository;

import jakarta.transaction.Transactional;
import mk.ukim.finki.aibotbackend.config.JpaConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * TODO(student): Test your session queries here, following the pattern from
 * {@link UserRepositoryTest}. Remove @Disabled once implemented.
 */
@DataJpaTest
@Import(JpaConfig.class)
@Transactional
@Testcontainers
@Disabled("TODO(student): Implement the extraction session repository tests.")
public class ExtractionSessionRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("aibot_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testFindSessions() {
        // TODO(student)
    }
}
