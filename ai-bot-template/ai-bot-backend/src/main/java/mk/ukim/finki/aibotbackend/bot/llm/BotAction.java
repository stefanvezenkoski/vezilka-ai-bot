package mk.ukim.finki.aibotbackend.bot.llm;

import mk.ukim.finki.aibotbackend.model.enums.BotActionType;

/**
 * A single concrete action chosen by the LLM.
 *
 * @param type      what to do
 * @param target    what to do it to — a URL for NAVIGATE, an element
 *                  description for CLICK/TYPE, {@code null} otherwise
 * @param value     the text to type for TYPE, {@code null} otherwise
 * @param reasoning the model's short explanation of why it chose this action
 *                  (persisted in the {@code BotActionLog} for traceability)
 */
public record BotAction(
    BotActionType type,
    String target,
    String value,
    String reasoning
) {
}
