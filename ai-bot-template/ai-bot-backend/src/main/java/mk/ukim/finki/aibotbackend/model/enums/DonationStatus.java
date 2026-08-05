package mk.ukim.finki.aibotbackend.model.enums;

/**
 * Lifecycle of a {@code DonationBatch}:
 * DRAFT -> APPROVED -> SUBMITTED -> ACCEPTED | REJECTED | FAILED
 */
public enum DonationStatus {
    DRAFT,
    APPROVED,
    SUBMITTED,
    ACCEPTED,
    REJECTED,
    FAILED
}
