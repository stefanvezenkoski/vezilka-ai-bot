package mk.ukim.finki.aibotbackend.bot.core;

import mk.ukim.finki.aibotbackend.bot.browser.BrowserAgent;
import mk.ukim.finki.aibotbackend.bot.extraction.ContentExtractor;
import mk.ukim.finki.aibotbackend.bot.extraction.LanguageDetector;
import mk.ukim.finki.aibotbackend.bot.llm.LlmClient;
import mk.ukim.finki.aibotbackend.config.BotProperties;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;
import org.springframework.stereotype.Component;

/**
 * Placeholder so the application boots before the assignment is implemented.
 *
 * <p>TODO(student): Replace this bean with a bot for YOUR assigned social
 * network, e.g. {@code InstagramBot extends AbstractSocialNetworkBot}, and
 * implement {@link #network()}, {@link #login()} and
 * {@link #buildGoal(ExtractionTarget)}. Do not override
 * {@code execute(...)} — the loop is shared.</p>
 */
@Component
public class StubSocialNetworkBot extends AbstractSocialNetworkBot {
    public StubSocialNetworkBot(
        BrowserAgent browserAgent,
        LlmClient llmClient,
        ContentExtractor contentExtractor,
        LanguageDetector languageDetector,
        BotProperties botProperties
    ) {
        super(browserAgent, llmClient, contentExtractor, languageDetector, botProperties);
    }

    @Override
    public SocialNetwork network() {
        throw new UnsupportedOperationException("TODO(student): Return your assigned social network.");
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException("TODO(student): Implement the network-specific login flow.");
    }

    @Override
    protected String buildGoal(ExtractionTarget target) {
        throw new UnsupportedOperationException("TODO(student): Build the extraction goal for a target.");
    }
}
