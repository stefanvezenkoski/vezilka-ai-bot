package mk.ukim.finki.aibotbackend.bot.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import mk.ukim.finki.aibotbackend.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Playwright-backed BrowserAgent implementation.
 * It drives a real Chromium instance to interact with the target site.
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
        if (playwright != null) {
            log.warn("Browser already started.");
            return;
        }
        log.info("Starting Playwright browser (headless = {})...", botProperties.headless());
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
        log.info("Playwright browser started successfully with stealth headers.");
    }

    @Override
    public synchronized void navigateTo(String url) {
        ensureStarted();
        log.info("Navigating to: {}", url);
        try {
            page.navigate(url);
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
            page.click("text=" + elementDescription);
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
            page.fill("text=" + elementDescription, text);
        }
    }

    @Override
    public synchronized void scrollDown() {
        ensureStarted();
        log.info("Scrolling down...");
        page.evaluate("window.scrollBy(0, window.innerHeight);");
    }

    @Override
    public synchronized byte[] takeScreenshot() {
        ensureStarted();
        log.info("Taking screenshot...");
        return page.screenshot(new Page.ScreenshotOptions().setType(com.microsoft.playwright.options.ScreenshotType.PNG));
    }

    @Override
    public synchronized PageSnapshot snapshot() {
        ensureStarted();
        log.info("Capturing page snapshot...");
        String url = page.url();
        String title = page.title();
        String domContent = page.content();
        
        byte[] screenshotBytes = null;
        try {
            screenshotBytes = takeScreenshot();
        } catch (Exception e) {
            log.warn("Failed to capture screenshot for snapshot", e);
        }
        
        String base64Screenshot = screenshotBytes != null ? 
                Base64.getEncoder().encodeToString(screenshotBytes) : null;

        return new PageSnapshot(url, title, domContent, base64Screenshot);
    }

    @Override
    public synchronized void close() {
        log.info("Closing Playwright browser...");
        try {
            if (page != null) {
                page.close();
                page = null;
            }
            if (context != null) {
                context.close();
                context = null;
            }
            if (browser != null) {
                browser.close();
                browser = null;
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            log.info("Browser closed successfully.");
        } catch (Exception e) {
            log.error("Error closing browser", e);
        }
    }

    private void ensureStarted() {
        if (playwright == null || page == null) {
            start();
        }
    }
}