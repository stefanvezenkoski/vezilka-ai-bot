package mk.ukim.finki.aibotbackend.integration.vezilka;

/**
 * The payload of one text donation towards doniraj.vezilka.ai.
 *
 * @param title     a short human-readable title of the donation
 *                  (e.g. "Macedonian posts from Reddit, July 2026")
 * @param content   the donated Macedonian text
 * @param sourceUrl where the content was found, for provenance
 */
public record TextDonationRequest(
    String title,
    String content,
    String sourceUrl
) {
}
