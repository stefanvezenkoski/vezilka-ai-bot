package mk.ukim.finki.aibotbackend.model.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("A user with the username '%s' already exists.".formatted(username));
    }
}
