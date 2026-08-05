package mk.ukim.finki.aibotbackend.model.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("A user with the username '%s' does not exist.".formatted(username));
    }
}
