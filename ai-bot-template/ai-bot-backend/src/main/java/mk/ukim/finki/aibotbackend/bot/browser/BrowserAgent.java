package mk.ukim.finki.aibotbackend.bot.browser;

/**
 * The browser automation seam of the bot: everything the agentic loop can
 * physically do inside a real browser.
 *
 * <p>TODO(student): Provide an implementation backed by a browser automation
 * library of your choice (Playwright for Java or Selenium are recommended).
 * The implementation must honour {@code BotProperties.headless()}.</p>
 *
 * <p>Element parameters are intentionally free-form <i>descriptions</i>
 * (e.g. "the search input in the top navigation bar", or a CSS selector) —
 * the implementation decides how to resolve them, which allows both
 * selector-based and LLM-grounded implementations.</p>
 */
public interface BrowserAgent {
    /**
     * Starts the underlying browser. Must be called before any other method.
     */
    void start();

    void navigateTo(String url);

    void click(String elementDescription);

    void type(String elementDescription, String text);

    void scrollDown();

    /**
     * @return a PNG screenshot of the current viewport
     */
    byte[] takeScreenshot();

    /**
     * Captures what is currently on screen so the {@code LlmClient} can decide
     * the next action and the {@code ContentExtractor} can extract content.
     */
    PageSnapshot snapshot();

    /**
     * Closes the underlying browser and releases all resources.
     */
    void close();
}
