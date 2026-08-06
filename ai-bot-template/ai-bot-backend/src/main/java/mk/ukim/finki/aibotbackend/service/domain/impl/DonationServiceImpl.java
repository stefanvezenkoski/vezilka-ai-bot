package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import mk.ukim.finki.aibotbackend.integration.vezilka.DonationReceipt;
import mk.ukim.finki.aibotbackend.integration.vezilka.TextDonationRequest;
import mk.ukim.finki.aibotbackend.integration.vezilka.VezilkaClient;
import mk.ukim.finki.aibotbackend.model.domain.DonationBatch;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.enums.DonationStatus;
import mk.ukim.finki.aibotbackend.repository.DonationBatchRepository;
import mk.ukim.finki.aibotbackend.service.domain.DonationService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationBatchRepository donationBatchRepository;
    private final ExtractedPostService extractedPostService;
    private final VezilkaClient vezilkaClient;

    public DonationServiceImpl(
        DonationBatchRepository donationBatchRepository,
        ExtractedPostService extractedPostService,
        VezilkaClient vezilkaClient
    ) {
        this.donationBatchRepository = donationBatchRepository;
        this.extractedPostService = extractedPostService;
        this.vezilkaClient = vezilkaClient;
    }

    @Override
    public List<DonationBatch> findAll() {
        return donationBatchRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Optional<DonationBatch> findById(Long id) {
        return donationBatchRepository.findById(id);
    }

    @Override
    @Transactional
    public DonationBatch createBatch(List<Long> postIds) {
        List<ExtractedPost> posts = extractedPostService.findAllById(postIds);

        DonationBatch batch = new DonationBatch(DonationStatus.DRAFT);
        DonationBatch savedBatch = donationBatchRepository.save(batch);

        for (ExtractedPost post : posts) {
            post.setDonationBatch(savedBatch);
            savedBatch.getPosts().add(post);
        }

        extractedPostService.saveAll(posts);
        return donationBatchRepository.save(savedBatch);
    }

    @Override
    public DonationBatch approve(Long id) {
        DonationBatch batch = donationBatchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DonationBatch not found: " + id));

        batch.setStatus(DonationStatus.APPROVED);
        return donationBatchRepository.save(batch);
    }

    @Override
    @Transactional
    public DonationBatch submit(Long id) {
        DonationBatch batch = donationBatchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DonationBatch not found: " + id));

        StringBuilder aggregatedContent = new StringBuilder();
        String mainSourceUrl = "https://kajgana.com";

        for (ExtractedPost post : batch.getPosts()) {
            if (post.getContent() != null) {
                aggregatedContent.append(post.getContent()).append("\n\n---\n\n");
            }
            if (post.getSourceUrl() != null && !post.getSourceUrl().isBlank()) {
                mainSourceUrl = post.getSourceUrl();
            }
        }

        TextDonationRequest request = new TextDonationRequest(
                "Macedonian Content Donation #" + id,
                aggregatedContent.toString().trim(),
                mainSourceUrl
        );

        DonationReceipt receipt = vezilkaClient.submitTextDonation(request);

        batch.setVezilkaReference(receipt.reference());
        batch.setSubmittedAt(LocalDateTime.now());
        batch.setStatus(DonationStatus.SUBMITTED);

        return donationBatchRepository.save(batch);
    }

    @Override
    @Transactional
    public void refreshSubmittedStatuses() {
        List<DonationBatch> submittedBatches = donationBatchRepository.findAll()
                .stream()
                .filter(b -> b.getStatus() == DonationStatus.SUBMITTED && b.getVezilkaReference() != null)
                .toList();

        for (DonationBatch batch : submittedBatches) {
            try {
                DonationStatus newStatus = vezilkaClient.checkStatus(batch.getVezilkaReference());
                if (newStatus != null && newStatus != batch.getStatus()) {
                    batch.setStatus(newStatus);
                    donationBatchRepository.save(batch);
                }
            } catch (Exception e) {
                // Log and continue checking other submitted batches
            }
        }
    }
}
