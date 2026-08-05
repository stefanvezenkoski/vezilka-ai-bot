package mk.ukim.finki.aibotbackend.integration.vezilka;

/**
 * What doniraj.vezilka.ai returns for a submitted donation.
 *
 * @param reference the identifier of the donation on the Vezilka side,
 *                  stored as {@code DonationBatch.vezilkaReference}
 * @param message   an optional human-readable status message
 */
public record DonationReceipt(
    String reference,
    String message
) {
}
