package mk.ukim.finki.aibotbackend.model.enums;

/**
 * Lifecycle of an {@code ExtractionSession}:
 * CREATED -> RUNNING -> (PAUSED -> RUNNING)* -> COMPLETED | FAILED
 */
public enum SessionStatus {
    CREATED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED
}
