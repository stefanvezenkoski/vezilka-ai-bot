package mk.ukim.finki.aibotbackend.integration.vezilka;

import mk.ukim.finki.aibotbackend.model.enums.DonationStatus;
import org.springframework.stereotype.Component;

/**
 * Placeholder so the application boots before the assignment is implemented.
 * TODO(student): Replace this bean with a real doniraj.vezilka.ai client.
 */
@Component
public class StubVezilkaClient implements VezilkaClient {
    private final VezilkaProperties vezilkaProperties;

    public StubVezilkaClient(VezilkaProperties vezilkaProperties) {
        this.vezilkaProperties = vezilkaProperties;
    }

    @Override
    public DonationReceipt submitTextDonation(TextDonationRequest request) {
        throw new UnsupportedOperationException("TODO(student): Implement VezilkaClient.submitTextDonation().");
    }

    @Override
    public DonationStatus checkStatus(String vezilkaReference) {
        throw new UnsupportedOperationException("TODO(student): Implement VezilkaClient.checkStatus().");
    }
}
