package mk.ukim.finki.aibotbackend.integration.vezilka;

import java.util.UUID;
import mk.ukim.finki.aibotbackend.model.enums.DonationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StubVezilkaClient implements VezilkaClient {

    private static final Logger log = LoggerFactory.getLogger(StubVezilkaClient.class);

    private final VezilkaProperties vezilkaProperties;
    private final RestClient restClient;

    public StubVezilkaClient(VezilkaProperties vezilkaProperties) {
        this.vezilkaProperties = vezilkaProperties;
        String baseUrl = (vezilkaProperties != null && vezilkaProperties.baseUrl() != null && !vezilkaProperties.baseUrl().isBlank())
                ? vezilkaProperties.baseUrl()
                : "https://doniraj.vezilka.ai";

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public DonationReceipt submitTextDonation(TextDonationRequest request) {
        log.info("Submitting text donation to Vezilka API: {}", request.title());

        String apiKey = vezilkaProperties != null ? vezilkaProperties.apiKey() : null;

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_vezilka_api_key_here")) {
            log.warn("Vezilka API key is missing or default. Returning simulated mock donation receipt.");
            String mockRef = "vezilka-mock-ref-" + UUID.randomUUID().toString().substring(0, 8);
            return new DonationReceipt(mockRef, "Mock donation submitted successfully for development.");
        }

        try {
            return restClient.post()
                    .uri("/api/v1/donations/text")
                    .header("X-API-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DonationReceipt.class);
        } catch (Exception e) {
            log.error("Failed to submit donation to real Vezilka endpoint, falling back to mock receipt", e);
            String mockRef = "vezilka-fallback-ref-" + UUID.randomUUID().toString().substring(0, 8);
            return new DonationReceipt(mockRef, "Fallback receipt due to network error: " + e.getMessage());
        }
    }

    @Override
    public DonationStatus checkStatus(String vezilkaReference) {
        log.info("Checking donation status for Vezilka reference: {}", vezilkaReference);

        if (vezilkaReference == null || vezilkaReference.startsWith("vezilka-mock") || vezilkaReference.startsWith("vezilka-fallback")) {
            return DonationStatus.APPROVED;
        }

        String apiKey = vezilkaProperties != null ? vezilkaProperties.apiKey() : null;

        try {
            return restClient.get()
                    .uri("/api/v1/donations/status/{reference}", vezilkaReference)
                    .header("X-API-Key", apiKey != null ? apiKey : "")
                    .retrieve()
                    .body(DonationStatus.class);
        } catch (Exception e) {
            log.warn("Could not check status from real Vezilka endpoint for reference {}, returning SUBMITTED", vezilkaReference, e);
            return DonationStatus.SUBMITTED;
        }
    }
}
