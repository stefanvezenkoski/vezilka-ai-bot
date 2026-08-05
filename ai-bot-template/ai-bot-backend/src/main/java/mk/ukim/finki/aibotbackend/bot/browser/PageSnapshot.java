package mk.ukim.finki.aibotbackend.bot.browser;

/**
 * What the bot "sees" at one moment: the current page of the social network,
 * captured by the {@link BrowserAgent} and handed to the {@code LlmClient}
 * (decision making) and the {@code ContentExtractor} (data extraction).
 *
 * @param url              the current URL of the page
 * @param title            the current page title
 * @param domContent       a textual representation of the page suitable for an LLM
 *                         prompt (raw/cleaned HTML, an accessibility tree, ...);
 *                         the implementation decides the exact format
 * @param screenshotBase64 an optional base64-encoded screenshot for
 *                         vision-capable LLMs; may be {@code null}
 */
public record PageSnapshot(
    String url,
    String title,
    String domContent,
    String screenshotBase64
) {
}
