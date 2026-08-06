package mk.ukim.finki.aibotbackend.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.domain.DonationBatch;
import mk.ukim.finki.aibotbackend.model.enums.DonationStatus;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;
import mk.ukim.finki.aibotbackend.repository.ExtractionSessionRepository;
import mk.ukim.finki.aibotbackend.service.domain.DonationService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
public class DonationServiceIntegrationTest {

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
    private DonationService donationService;

    @Autowired
    private ExtractedPostService extractedPostService;

    @Autowired
    private ExtractionSessionRepository extractionSessionRepository;

    @Test
    void testDonationWorkflow() {
        // 1. Create a session & post
        ExtractionSession session = extractionSessionRepository.save(new ExtractionSession(SocialNetwork.KAJGANA, "Test Session"));
        ExtractedPost post = new ExtractedPost(
                session,
                "ext-101",
                "Kajgana User",
                "Ова е македонски текст за донација.",
                "https://forum.kajgana.com/thread/1",
                LocalDateTime.now(),
                0.95
        );
        List<ExtractedPost> savedPosts = extractedPostService.saveAll(List.of(post));
        assertThat(savedPosts).isNotEmpty();
        Long postId = savedPosts.get(0).getId();

        // 2. Create batch
        DonationBatch batch = donationService.createBatch(List.of(postId));
        assertThat(batch.getId()).isNotNull();
        assertThat(batch.getStatus()).isEqualTo(DonationStatus.DRAFT);
        assertThat(batch.getPosts()).hasSize(1);

        // 3. Approve batch
        DonationBatch approved = donationService.approve(batch.getId());
        assertThat(approved.getStatus()).isEqualTo(DonationStatus.APPROVED);

        // 4. Submit batch
        DonationBatch submitted = donationService.submit(approved.getId());
        assertThat(submitted.getStatus()).isEqualTo(DonationStatus.SUBMITTED);
        assertThat(submitted.getVezilkaReference()).isNotNull();
        assertThat(submitted.getSubmittedAt()).isNotNull();
    }
}
