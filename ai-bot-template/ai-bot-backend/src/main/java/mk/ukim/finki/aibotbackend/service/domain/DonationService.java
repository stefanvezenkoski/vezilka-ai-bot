package mk.ukim.finki.aibotbackend.service.domain;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.domain.DonationBatch;

/**
 * Domain service for donation batches and their journey to doniraj.vezilka.ai.
 */
public interface DonationService {
    List<DonationBatch> findAll();

    Optional<DonationBatch> findById(Long id);

    /**
     * Creates a DRAFT batch from already-extracted posts.
     * Throws {@code PostNotFoundException} when an id does not exist.
     */
    DonationBatch createBatch(List<Long> postIds);

    /**
     * Transitions a DRAFT batch to APPROVED — a human has reviewed the
     * content and confirmed it should be donated.
     */
    DonationBatch approve(Long id);

    /**
     * Submits an APPROVED batch to doniraj.vezilka.ai via the
     * {@code VezilkaClient}, stores the returned reference, stamps
     * {@code submittedAt} and transitions the batch to SUBMITTED.
     */
    DonationBatch submit(Long id);

    /**
     * Polls Vezilka for every SUBMITTED batch and updates its status
     * (ACCEPTED / REJECTED). Called periodically by the
     * {@code DonationStatusScheduler}.
     */
    void refreshSubmittedStatuses();
}
