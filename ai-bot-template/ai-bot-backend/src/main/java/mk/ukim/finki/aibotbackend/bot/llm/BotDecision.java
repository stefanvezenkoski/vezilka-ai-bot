package mk.ukim.finki.aibotbackend.bot.llm;

/**
 * The outcome of one LLM decision step.
 *
 * @param action      the next action to perform; ignored when {@code goalReached} is {@code true}
 * @param goalReached {@code true} when the model judges that the goal for the
 *                    current target has been achieved and the loop should stop
 * @param rationale   the model's overall reasoning for this decision
 */
public record BotDecision(
    BotAction action,
    boolean goalReached,
    String rationale
) {
}
