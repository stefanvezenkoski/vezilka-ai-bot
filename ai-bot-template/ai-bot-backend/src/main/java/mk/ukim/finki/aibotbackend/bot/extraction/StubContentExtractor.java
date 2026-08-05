package mk.ukim.finki.aibotbackend.bot.extraction;

import java.util.List;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;
import org.springframework.stereotype.Component;

/**
 * Placeholder so the application boots before the assignment is implemented.
 * TODO(student): Replace this bean with an extractor for your assigned social network.
 */
@Component
public class StubContentExtractor implements ContentExtractor {
    @Override
    public List<CreateExtractedPostDto> extract(PageSnapshot snapshot) {
        throw new UnsupportedOperationException("TODO(student): Implement ContentExtractor.extract().");
    }
}
