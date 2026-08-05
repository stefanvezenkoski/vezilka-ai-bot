package mk.ukim.finki.aibotbackend.model.exception;

public class DonationBatchNotFoundException extends RuntimeException {
    public DonationBatchNotFoundException(Long id) {
        super("A donation batch with id %d does not exist.".formatted(id));
    }
}
