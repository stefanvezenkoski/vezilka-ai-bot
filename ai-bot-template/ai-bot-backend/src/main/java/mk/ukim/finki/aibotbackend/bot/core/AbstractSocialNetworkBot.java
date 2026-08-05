package mk.ukim.finki.aibotbackend.bot.core;

import java.util.ArrayList;
import java.util.List;
import mk.ukim.finki.aibotbackend.bot.browser.BrowserAgent;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import mk.ukim.finki.aibotbackend.bot.extraction.ContentExtractor;
import mk.ukim.finki.aibotbackend.bot.extraction.LanguageDetector;
import mk.ukim.finki.aibotbackend.bot.llm.BotAction;
import mk.ukim.finki.aibotbackend.bot.llm.BotDecision;
import mk.ukim.finki.aibotbackend.bot.llm.LlmClient;
import mk.ukim.finki.aibotbackend.config.BotProperties;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.enums.BotActionType;

/**
 * The generic perceive-decide-act loop, shared by every bot implementation.
 *
 * <p>The loop is intentionally {@code final}: the assignment is NOT to change
 * how the agent iterates, but to implement the seams it is built from
 * ({@link BrowserAgent}, {@link LlmClient}, {@link ContentExtractor},
 * {@link LanguageDetector}) and the network-specific hooks
 * ({@link #network()}, {@link #login()}, {@link #buildGoal(ExtractionTarget)}).</p>
 */
public abstract class AbstractSocialNetworkBot implements SocialNetworkBot {
    protected final BrowserAgent browserAgent;
    protected final LlmClient llmClient;
    protected final ContentExtractor contentExtractor;
    protected final LanguageDetector languageDetector;
    protected final BotProperties botProperties;

    protected AbstractSocialNetworkBot(
        BrowserAgent browserAgent,
        LlmClient llmClient,
        ContentExtractor contentExtractor,
        LanguageDetector languageDetector,
        BotProperties botProperties
    ) {
        this.browserAgent = browserAgent;
        this.llmClient = llmClient;
        this.contentExtractor = contentExtractor;
        this.languageDetector = languageDetector;
        this.botProperties = botProperties;
    }

    /**
     * Translates an extraction target into the natural-language goal handed to
     * the {@link LlmClient} on every iteration, e.g. "Open the profile
     * '@makedonska.poezija' and extract its most recent public posts".
     */
    protected abstract String buildGoal(ExtractionTarget target);

    @Override
    public final List<CreateExtractedPostDto> execute(ExtractionTarget target, BotStepListener stepListener) {
        String goal = buildGoal(target);
        List<CreateExtractedPostDto> collected = new ArrayList<>();
        List<BotAction> history = new ArrayList<>();

        for (int step = 0; step < botProperties.maxStepsPerTarget(); step++) {
            PageSnapshot snapshot = browserAgent.snapshot();
            BotDecision decision = llmClient.decideNextAction(snapshot, goal, history);
            if (decision.goalReached()) {
                break;
            }

            BotAction action = decision.action();
            boolean successful = true;
            try {
                perform(action, snapshot, collected);
            } catch (RuntimeException exception) {
                successful = false;
            }
            history.add(action);
            stepListener.onStep(action, successful);

            if (action.type() == BotActionType.FINISH) {
                break;
            }
        }
        return collected;
    }

    private void perform(BotAction action, PageSnapshot snapshot, List<CreateExtractedPostDto> collected) {
        switch (action.type()) {
            case NAVIGATE -> browserAgent.navigateTo(action.target());
            case CLICK -> browserAgent.click(action.target());
            case TYPE -> browserAgent.type(action.target(), action.value());
            case SCROLL -> browserAgent.scrollDown();
            case WAIT -> waitBriefly();
            case LOGIN -> login();
            case EXTRACT -> collected.addAll(extractFrom(snapshot));
            case FINISH -> {
            }
        }
    }

    private List<CreateExtractedPostDto> extractFrom(PageSnapshot snapshot) {
        return contentExtractor
            .extract(snapshot)
            .stream()
            .map(post -> post.withMacedonianConfidence(
                post.content() == null ? 0.0 : languageDetector.macedonianConfidence(post.content())
            ))
            .toList();
    }

    private void waitBriefly() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        browserAgent.close();
    }
}
