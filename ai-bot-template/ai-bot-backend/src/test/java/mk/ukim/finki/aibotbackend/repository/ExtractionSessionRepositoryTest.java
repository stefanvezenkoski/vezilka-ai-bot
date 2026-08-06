package mk.ukim.finki.aibotbackend.repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.config.JpaConfig;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.enums.SessionStatus;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@Transactional
@Testcontainers
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

    @Autowired
    private ExtractionSessionRepository extractionSessionRepository;

    private ExtractionSession savedSession;

    @BeforeEach
    void setUp() {
        ExtractionSession session = new ExtractionSession(SocialNetwork.KAJGANA, "Test Kajgana Session");
        savedSession = extractionSessionRepository.save(session);
    }

    @Test
    void testFindSessions() {
        Optional<ExtractionSession> result = extractionSessionRepository.findById(savedSession.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getSocialNetwork()).isEqualTo(SocialNetwork.KAJGANA);
        assertThat(result.get().getDescription()).isEqualTo("Test Kajgana Session");
        assertThat(result.get().getStatus()).isEqualTo(SessionStatus.CREATED);
    }

    @Test
    void testSaveSessionStatusChange() {
        savedSession.setStatus(SessionStatus.RUNNING);
        ExtractionSession updated = extractionSessionRepository.save(savedSession);
        assertThat(updated.getStatus()).isEqualTo(SessionStatus.RUNNING);
    }
}
