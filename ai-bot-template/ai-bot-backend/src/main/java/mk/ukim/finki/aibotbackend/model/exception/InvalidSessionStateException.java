package mk.ukim.finki.aibotbackend.model.exception;

import mk.ukim.finki.aibotbackend.model.enums.SessionStatus;

public class InvalidSessionStateException extends RuntimeException {
    public InvalidSessionStateException(Long id, SessionStatus status) {
        super("The extraction session with id %d cannot perform this operation in status %s.".formatted(id, status));
    }
}
