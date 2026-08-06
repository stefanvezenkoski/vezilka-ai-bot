package mk.ukim.finki.aibotbackend.bot.extraction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.dto.CreateMediaItemDto;
import mk.ukim.finki.aibotbackend.model.enums.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Extractor backed by regex/DOM block parsing for Kajgana.mk and Forum.Kajgana.com.
 */
@Component
public class StubContentExtractor implements ContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(StubContentExtractor.class);

    private static final Pattern ARTICLE_BLOCK_PATTERN = Pattern.compile("<article[^>]*>(.*?)</article>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern FORUM_POST_PATTERN = Pattern.compile("<(?:div|li)[^>]*(?:class|id)=[\"'][^\"']*(?:message|post|thread)[^\"']*[\"'][^>]*>(.*?)</(?:div|li)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern TITLE_PATTERN = Pattern.compile("<h[1-3][^>]*>(.*?)</h[1-3]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern A_HREF_PATTERN = Pattern.compile("<a[^>]+href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    @Override
    public List<CreateExtractedPostDto> extract(PageSnapshot snapshot) {
        log.info("Extracting content from snapshot URL: {}", snapshot.url());
        List<CreateExtractedPostDto> posts = new ArrayList<>();

        if (snapshot.domContent() == null || snapshot.domContent().isBlank()) {
            return posts;
        }

        String html = snapshot.domContent();
        String currentUrl = snapshot.url() != null ? snapshot.url() : "https://kajgana.com";

        // Extract articles (Kajgana news/blog posts)
        Matcher articleMatcher = ARTICLE_BLOCK_PATTERN.matcher(html);
        while (articleMatcher.find()) {
            String blockHtml = articleMatcher.group(1);
            CreateExtractedPostDto dto = parseBlock(blockHtml, currentUrl, snapshot.title());
            if (dto != null) {
                posts.add(dto);
            }
        }

        // Extract forum posts if no articles found or if on forum domain
        if (posts.isEmpty() || currentUrl.contains("forum.kajgana")) {
            Matcher forumMatcher = FORUM_POST_PATTERN.matcher(html);
            while (forumMatcher.find()) {
                String blockHtml = forumMatcher.group(1);
                CreateExtractedPostDto dto = parseBlock(blockHtml, currentUrl, snapshot.title());
                if (dto != null) {
                    posts.add(dto);
                }
            }
        }

        // Fallback: If no structured blocks matched, extract title and paragraphs from full body
        if (posts.isEmpty()) {
            String cleanText = html.replaceAll("<script[^>]*>(.*?)</script>", "")
                    .replaceAll("<style[^>]*>(.*?)</style>", "")
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (cleanText.length() > 50) {
                String postContent = cleanText.length() > 2000 ? cleanText.substring(0, 2000) : cleanText;
                posts.add(new CreateExtractedPostDto(
                        null,
                        "Kajgana User",
                        postContent,
                        currentUrl,
                        LocalDateTime.now(),
                        null,
                        List.of()
                ));
            }
        }

        log.info("Extracted {} posts from snapshot.", posts.size());
        return posts;
    }

    private CreateExtractedPostDto parseBlock(String blockHtml, String baseUrl, String defaultTitle) {
        String title = defaultTitle;
        Matcher titleMatcher = TITLE_PATTERN.matcher(blockHtml);
        if (titleMatcher.find()) {
            title = stripHtml(titleMatcher.group(1));
        }

        StringBuilder contentBuilder = new StringBuilder();
        Matcher pMatcher = PARAGRAPH_PATTERN.matcher(blockHtml);
        while (pMatcher.find()) {
            String text = stripHtml(pMatcher.group(1));
            if (!text.isBlank()) {
                contentBuilder.append(text).append("\n\n");
            }
        }

        String contentText = contentBuilder.toString().trim();
        if (contentText.isBlank()) {
            contentText = stripHtml(blockHtml);
        }

        if (contentText.length() < 20) {
            return null; // Skip tiny irrelevant snippets
        }

        // Extract links & images
        List<CreateMediaItemDto> mediaItems = new ArrayList<>();
        Matcher imgMatcher = IMG_PATTERN.matcher(blockHtml);
        while (imgMatcher.find()) {
            String imgSrc = imgMatcher.group(1);
            if (imgSrc.startsWith("http") || imgSrc.startsWith("//")) {
                mediaItems.add(new CreateMediaItemDto(MediaType.IMAGE, imgSrc, "Article Image"));
            }
        }

        String sourceUrl = baseUrl;
        Matcher hrefMatcher = A_HREF_PATTERN.matcher(blockHtml);
        if (hrefMatcher.find()) {
            String href = hrefMatcher.group(1);
            if (href.startsWith("http")) {
                sourceUrl = href;
            }
        }

        return new CreateExtractedPostDto(
                null,
                "Kajgana User",
                contentText,
                sourceUrl,
                LocalDateTime.now(),
                null,
                mediaItems
        );
    }

    private String stripHtml(String input) {
        if (input == null) return "";
        return input.replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
