package mk.ukim.finki.aibotbackend.bot.llm;

import java.util.List;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import org.springframework.stereotype.Component;

/**
 * Placeholder so the application boots before the assignment is implemented.
 * TODO(student): Replace this bean with an implementation backed by a real LLM.
 */
@Component
public class StubLlmClient implements LlmClient {
    @Override
    public String complete(String systemPrompt, String userPrompt) {
        throw new UnsupportedOperationException("TODO(student): Implement LlmClient.complete().");
    }

    @Override
    public BotDecision decideNextAction(PageSnapshot snapshot, String goal, List<BotAction> history) {
        throw new UnsupportedOperationException("TODO(student): Implement LlmClient.decideNextAction().");
    }
}
