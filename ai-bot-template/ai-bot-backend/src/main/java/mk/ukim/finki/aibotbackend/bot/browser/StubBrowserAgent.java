package mk.ukim.finki.aibotbackend.bot.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import mk.ukim.finki.aibotbackend.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Playwright-backed BrowserAgent implementation.
 * Robustly manages browser lifecycle, auto-recovering from closed page/browser states.
 */
@Component
public class StubBrowserAgent implements BrowserAgent {

    private static final Logger log = LoggerFactory.getLogger(StubBrowserAgent.class);

    private final BotProperties botProperties;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    public StubBrowserAgent(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    @Override
    public synchronized void start() {
        if (playwright != null && browser != null && browser.isConnected() && page != null && !page.isClosed()) {
            log.info("Browser is already running and active.");
            return;
        }

        close();

        log.info("Starting fresh Playwright browser (headless = {})...", botProperties.headless());
        playwright = Playwright.create();

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(botProperties.headless());

        browser = playwright.chromium().launch(options);
        context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1280, 800)
                .setLocale("mk-MK")
                .setIgnoreHTTPSErrors(true)
        );
        page = context.newPage();
        log.info("Playwright browser started successfully.");
    }

    @Override
    public synchronized void navigateTo(String url) {
        ensureStarted();
        log.info("Navigating to: {}", url);
        try {
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(60000));
        } catch (Exception e) {
            log.warn("Failed to navigate to URL: {}. Continuing crawler...", url, e);
        }
    }

    @Override
    public synchronized void click(String elementDescription) {
        ensureStarted();
        log.info("Clicking element: {}", elementDescription);
        try {
            page.locator(elementDescription).first().click();
        } catch (Exception e) {
            log.warn("Failed to click element using description as raw locator, trying text search: {}", elementDescription, e);
            try {
                page.click("text=" + elementDescription);
            } catch (Exception ex) {
                log.warn("Could not click text fallback: {}", elementDescription, ex);
            }
        }
    }

    @Override
    public synchronized void type(String elementDescription, String text) {
        ensureStarted();
        log.info("Typing text '{}' into element: {}", text, elementDescription);
        try {
            page.locator(elementDescription).first().fill(text);
        } catch (Exception e) {
            log.warn("Failed to fill element using description as raw locator, trying text search: {}", elementDescription, e);
            try {
                page.fill("text=" + elementDescription, text);
            } catch (Exception ex) {
                log.warn("Could not type into fallback element: {}", elementDescription, ex);
            }
        }
    }

    @Override
    public synchronized void scrollDown() {
        ensureStarted();
        log.info("Scrolling down...");
        try {
            page.evaluate("window.scrollBy(0, window.innerHeight);");
        } catch (Exception e) {
            log.warn("Scroll down failed", e);
        }
    }

    @Override
    public synchronized byte[] takeScreenshot() {
        ensureStarted();
        log.info("Taking screenshot...");
        try {
            return page.screenshot(new Page.ScreenshotOptions().setType(com.microsoft.playwright.options.ScreenshotType.PNG));
        } catch (Exception e) {
            log.warn("Failed to take screenshot", e);
            return new byte[0];
        }
    }

    @Override
    public synchronized PageSnapshot snapshot() {
        ensureStarted();
        log.info("Capturing page snapshot...");
        String url = page != null ? page.url() : "https://kajgana.com";
        String title = page != null ? page.title() : "Kajgana";
        String domContent = page != null ? page.content() : "";

        byte[] screenshotBytes = null;
        try {
            screenshotBytes = takeScreenshot();
        } catch (Exception e) {
            log.warn("Failed to capture screenshot for snapshot", e);
        }

        String base64Screenshot = (screenshotBytes != null && screenshotBytes.length > 0) ?
                Base64.getEncoder().encodeToString(screenshotBytes) : null;

        return new PageSnapshot(url, title, domContent, base64Screenshot);
    }

    @Override
    public synchronized void close() {
        log.info("Closing Playwright browser resources...");
        try {
            if (page != null && !page.isClosed()) {
                page.close();
            }
        } catch (Exception ignored) {}
        page = null;

        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception ignored) {}
        context = null;

        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
            }
        } catch (Exception ignored) {}
        browser = null;

        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception ignored) {}
        playwright = null;

        log.info("Browser resources closed cleanly.");
    }

    private void ensureStarted() {
        if (playwright == null || browser == null || !browser.isConnected() || page == null || page.isClosed()) {
            start();
        }
    }
}