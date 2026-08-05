package mk.ukim.finki.aibotbackend.model.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("An extracted post with id %d does not exist.".formatted(id));
    }
}
