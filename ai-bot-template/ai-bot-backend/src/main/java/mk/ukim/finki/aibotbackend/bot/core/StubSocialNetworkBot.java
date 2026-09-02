package mk.ukim.finki.aibotbackend.bot.core;

import mk.ukim.finki.aibotbackend.bot.browser.BrowserAgent;
import mk.ukim.finki.aibotbackend.bot.extraction.ContentExtractor;
import mk.ukim.finki.aibotbackend.bot.extraction.LanguageDetector;
import mk.ukim.finki.aibotbackend.bot.llm.LlmClient;
import mk.ukim.finki.aibotbackend.config.BotProperties;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SocialNetworkBot implementation for Kajgana.mk and Forum.Kajgana.com.
 * Strictly navigates to and extracts only from the user-specified target URL.
 */
@Component
public class StubSocialNetworkBot extends AbstractSocialNetworkBot {

    private static final Logger log = LoggerFactory.getLogger(StubSocialNetworkBot.class);

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
        return SocialNetwork.KAJGANA;
    }

    @Override
    public void login() {
        log.info("SocialNetworkBot initialized for target site execution.");
        // Do NOT force navigation to root homepage here, allowing bot to navigate directly to the specific target URL
    }

    @Override
    protected String buildGoal(ExtractionTarget target) {
        if (target == null || target.getValue() == null) {
            return "Navigate to https://kajgana.com and extract all Macedonian articles and discussions published between 1.8.2026 and 5.8.2026.";
        }

        return switch (target.getType()) {
            case FEED_URL, PROFILE -> {
                String url = target.getValue();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                yield "Navigate to " + url + " and extract ONLY Macedonian articles and discussions from this target published between 1.8.2026 and 5.8.2026.";
            }
            case KEYWORD, HASHTAG -> {
                String searchUrl = "https://kajgana.com/baraj?search=" + java.net.URLEncoder.encode(target.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                yield "Navigate to " + searchUrl + " and extract Macedonian discussions and articles related to '" + target.getValue() + "' published between 1.8.2026 and 5.8.2026.";
            }
        };
    }
}
