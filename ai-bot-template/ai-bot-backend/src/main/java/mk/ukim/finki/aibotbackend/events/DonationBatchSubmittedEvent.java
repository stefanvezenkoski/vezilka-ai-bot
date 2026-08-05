package mk.ukim.finki.aibotbackend.events;

/**
 * Published after a donation batch has been submitted to doniraj.vezilka.ai.
 */
public record DonationBatchSubmittedEvent(Long batchId) {
}
