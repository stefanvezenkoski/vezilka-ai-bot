package mk.ukim.finki.aibotbackend.bot.browser;

import org.springframework.stereotype.Component;

/**
 * Placeholder so the application boots before the assignment is implemented.
 * TODO(student): Replace this bean with your Playwright/Selenium-backed implementation.
 */
@Component
public class StubBrowserAgent implements BrowserAgent {
    @Override
    public void start() {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.start().");
    }

    @Override
    public void navigateTo(String url) {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.navigateTo().");
    }

    @Override
    public void click(String elementDescription) {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.click().");
    }

    @Override
    public void type(String elementDescription, String text) {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.type().");
    }

    @Override
    public void scrollDown() {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.scrollDown().");
    }

    @Override
    public byte[] takeScreenshot() {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.takeScreenshot().");
    }

    @Override
    public PageSnapshot snapshot() {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.snapshot().");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO(student): Implement BrowserAgent.close().");
    }
}
