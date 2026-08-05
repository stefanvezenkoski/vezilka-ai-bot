package mk.ukim.finki.aibotbackend.model.exception;

/**
 * Thrown by the bot layers ({@code BrowserAgent}, {@code LlmClient},
 * {@code SocialNetworkBot}) when a step of the agentic loop fails
 * unrecoverably.
 */
public class BotExecutionException extends RuntimeException {
    public BotExecutionException(String message) {
        super(message);
    }

    public BotExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
