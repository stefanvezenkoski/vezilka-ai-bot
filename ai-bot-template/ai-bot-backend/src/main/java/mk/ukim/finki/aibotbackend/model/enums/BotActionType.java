package mk.ukim.finki.aibotbackend.model.enums;

/**
 * The primitive actions the agentic loop can decide to perform.
 * The {@code LlmClient} chooses one of these on every iteration;
 * the {@code AbstractSocialNetworkBot} dispatches it onto the {@code BrowserAgent}.
 */
public enum BotActionType {
    NAVIGATE,
    CLICK,
    TYPE,
    SCROLL,
    WAIT,
    EXTRACT,
    LOGIN,
    FINISH
}
