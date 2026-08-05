package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.integration.vezilka.VezilkaClient;
import mk.ukim.finki.aibotbackend.model.domain.DonationBatch;
import mk.ukim.finki.aibotbackend.repository.DonationBatchRepository;
import mk.ukim.finki.aibotbackend.service.domain.DonationService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.springframework.stereotype.Service;

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
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.findAll().");
    }

    @Override
    public Optional<DonationBatch> findById(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.findById().");
    }

    @Override
    public DonationBatch createBatch(List<Long> postIds) {
        // TODO(student): Load the posts (extractedPostService.findAllById), create a
        //  DRAFT batch, attach the posts to it and save everything.
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.createBatch().");
    }

    @Override
    public DonationBatch approve(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.approve().");
    }

    @Override
    public DonationBatch submit(Long id) {
        // TODO(student): Build a TextDonationRequest from the batch content, call
        //  vezilkaClient.submitTextDonation, store the receipt reference, stamp
        //  submittedAt, set the status to SUBMITTED and save. Consider publishing
        //  a DonationBatchSubmittedEvent afterwards.
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.submit().");
    }

    @Override
    public void refreshSubmittedStatuses() {
        // TODO(student): For every batch in status SUBMITTED, call
        //  vezilkaClient.checkStatus(batch.getVezilkaReference()) and update the status.
        throw new UnsupportedOperationException("TODO(student): Implement DonationService.refreshSubmittedStatuses().");
    }
}
