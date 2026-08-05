package mk.ukim.finki.aibotbackend.bot.llm;

import java.util.List;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;

/**
 * The decision-making seam of the bot: a large language model that looks at
 * what the browser currently shows and decides what to do next.
 *
 * <p>TODO(student): Provide an implementation backed by an LLM provider of your
 * choice (a hosted API or a locally running model). Keep the provider-specific
 * details (API keys, model names, prompt formats) inside the implementation —
 * nothing outside this package may depend on them.</p>
 */
public interface LlmClient {
    /**
     * Low-level completion call, useful for auxiliary prompts
     * (e.g. summarising a page, classifying content).
     *
     * @return the model's raw text response
     */
    String complete(String systemPrompt, String userPrompt);

    /**
     * The core of the agentic loop: given the current page, the goal for the
     * current extraction target and the actions performed so far, decide what
     * the bot should do next.
     *
     * <p>Implementations typically serialise the snapshot and history into a
     * prompt, ask the model for a structured (e.g. JSON) response, and parse
     * it into a {@link BotDecision}.</p>
     */
    BotDecision decideNextAction(PageSnapshot snapshot, String goal, List<BotAction> history);
}
