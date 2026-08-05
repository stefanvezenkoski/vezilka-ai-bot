package mk.ukim.finki.aibotbackend.bot.core;

import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;

/**
 * An AI bot for exactly one social network.
 *
 * <p>Each student implements this for their assigned network by extending
 * {@link AbstractSocialNetworkBot}, which already contains the generic
 * perceive-decide-act loop. The template assumes exactly one
 * {@code SocialNetworkBot} bean in the application context.</p>
 */
public interface SocialNetworkBot {
    /**
     * @return the social network this bot knows how to navigate
     */
    SocialNetwork network();

    /**
     * Authenticates the bot within the social network (when the network
     * requires it for the targeted content). How credentials are supplied
     * is up to the implementation — environment variables are recommended;
     * never commit them.
     */
    void login();

    /**
     * Runs the agentic loop for one extraction target and returns the posts
     * collected along the way, annotated with Macedonian-language confidence.
     * Does not persist anything — the caller decides what to do with the result.
     */
    List<CreateExtractedPostDto> execute(ExtractionTarget target, BotStepListener stepListener);

    /**
     * Releases all resources (typically closes the browser).
     */
    void shutdown();
}
