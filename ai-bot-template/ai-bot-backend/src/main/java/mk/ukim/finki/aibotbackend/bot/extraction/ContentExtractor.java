package mk.ukim.finki.aibotbackend.bot.extraction;

import java.util.List;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;

/**
 * Turns a captured page into structured posts.
 *
 * <p>TODO(student): Provide an implementation for your assigned social network.
 * It may parse the DOM with selectors, use the {@code LlmClient} to extract
 * structured data from the page text, or combine both. The returned DTOs do
 * not need a language confidence — the agentic loop fills it in via the
 * {@link LanguageDetector}.</p>
 */
public interface ContentExtractor {
    List<CreateExtractedPostDto> extract(PageSnapshot snapshot);
}
