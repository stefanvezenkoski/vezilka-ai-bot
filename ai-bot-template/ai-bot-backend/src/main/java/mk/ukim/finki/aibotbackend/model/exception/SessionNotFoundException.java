package mk.ukim.finki.aibotbackend.model.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(Long id) {
        super("An extraction session with id %d does not exist.".formatted(id));
    }
}
