package mk.ukim.finki.aibotbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of the agentic loop, bound from the {@code bot.*} properties.
 *
 * @param maxStepsPerTarget hard upper bound of perceive-decide-act iterations the bot
 *                          may spend on a single {@code ExtractionTarget} before giving up
 * @param headless          whether the browser controlled by the {@code BrowserAgent}
 *                          should run without a visible window
 */
@ConfigurationProperties(prefix = "bot")
public record BotProperties(
    Integer maxStepsPerTarget,
    Boolean headless
) {
}
