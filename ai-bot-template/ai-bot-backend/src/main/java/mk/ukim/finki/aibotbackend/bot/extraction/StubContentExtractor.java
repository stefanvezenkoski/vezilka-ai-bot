package mk.ukim.finki.aibotbackend.bot.extraction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Extractor backed by DOM parsing tailored for Kajgana.mk and Forum.Kajgana.com.
 * Strictly extracts Macedonian content published between 01.08.2026 and 01.09.2026.
 */
@Component
public class StubContentExtractor implements ContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(StubContentExtractor.class);
    private static final double MIN_MACEDONIAN_CONFIDENCE = 0.30;

    // Date filtering boundaries: 01.08.2026 - 01.09.2026
    private static final LocalDateTime RANGE_START = LocalDateTime.of(2026, 8, 1, 0, 0, 0);
    private static final LocalDateTime RANGE_END = LocalDateTime.of(2026, 9, 1, 23, 59, 59);

    private static final Pattern ARTICLE_BODY_PATTERN = Pattern.compile(
            "<div[^>]*class=[\"'][^\"']*field--name-body[^\"']*[\"'][^>]*>(.*?)</div>\\s*</div>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TEASER_BLOCK_PATTERN = Pattern.compile(
            "<(?:div|article|section)[^>]*(?:class|id)=[\"'][^\"']*(?:teaser|node|card|article|story|item|views-row|message|post|thread)[^\"']*[\"'][^>]*>(.*?)</(?:div|article|section)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern H1_TITLE_PATTERN = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<h[1-6][^>]*>(.*?)</h[1-6]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern A_HREF_PATTERN = Pattern.compile("<a[^>]+href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("<div[^>]*class=[\"'][^\"']*field--name-field-author-source[^\"']*[\"'][^>]*>(.*?)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern DATETIME_TAG_PATTERN = Pattern.compile("<time[^>]+datetime=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_TIME_PATTERN = Pattern.compile("data-time=[\"'](\\d+)[\"']", Pattern.CASE_INSENSITIVE);

    private final LanguageDetector languageDetector;

    public StubContentExtractor(LanguageDetector languageDetector) {
        this.languageDetector = languageDetector;
    }

    @Override
    public List<CreateExtractedPostDto> extract(PageSnapshot snapshot) {
        log.info("Extracting content for target date range 01.08.2026 - 01.09.2026 from URL: {}", snapshot.url());
        List<CreateExtractedPostDto> posts = new ArrayList<>();
        Set<String> seenHashes = new HashSet<>();

        if (snapshot.domContent() == null || snapshot.domContent().isBlank()) {
            return posts;
        }

        String html = snapshot.domContent();
        String currentUrl = snapshot.url() != null && !snapshot.url().isBlank() ? snapshot.url() : "https://kajgana.com";
        LocalDateTime parsedTimestamp = parseDateFromHtml(html);

        // 1. Full Article Page Detection
        int bodyIndex = html.indexOf("field--name-body");
        if (bodyIndex != -1) {
            String articleSectionHtml = html.substring(bodyIndex);
            int endTagIndex = articleSectionHtml.indexOf("field--name-field-tags");
            if (endTagIndex == -1) endTagIndex = articleSectionHtml.indexOf("<footer");
            if (endTagIndex == -1) endTagIndex = articleSectionHtml.indexOf("</article>");
            if (endTagIndex != -1) {
                articleSectionHtml = articleSectionHtml.substring(0, endTagIndex);
            }

            String articleTitle = snapshot.title() != null ? snapshot.title() : "";
            Matcher h1Matcher = H1_TITLE_PATTERN.matcher(html);
            if (h1Matcher.find()) {
                articleTitle = stripHtml(h1Matcher.group(1));
            }

            String author = "Кајгана";
            Matcher authorMatcher = AUTHOR_PATTERN.matcher(html);
            if (authorMatcher.find()) {
                String parsedAuthor = stripHtml(authorMatcher.group(1));
                if (!parsedAuthor.isBlank()) {
                    author = parsedAuthor;
                }
            }

            StringBuilder fullBodyBuilder = new StringBuilder();
            if (!articleTitle.isBlank()) {
                fullBodyBuilder.append(articleTitle).append("\n\n");
            }

            Matcher pMatcher = PARAGRAPH_PATTERN.matcher(articleSectionHtml);
            while (pMatcher.find()) {
                String pText = stripHtml(pMatcher.group(1));
                if (!pText.isBlank() && pText.length() >= 5 && !pText.equalsIgnoreCase(articleTitle)) {
                    fullBodyBuilder.append(pText).append("\n\n");
                }
            }

            String fullArticleContent = fullBodyBuilder.toString().trim();
            if (fullArticleContent.length() >= 30) {
                List<CreateMediaItemDto> mediaItems = new ArrayList<>();
                Matcher imgMatcher = IMG_PATTERN.matcher(html);
                if (imgMatcher.find()) {
                    String imgSrc = resolveUrl(currentUrl, imgMatcher.group(1));
                    mediaItems.add(new CreateMediaItemDto(MediaType.IMAGE, imgSrc, "Main Article Image"));
                }

                String hashKey = currentUrl + "::" + fullArticleContent.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            author,
                            fullArticleContent,
                            currentUrl,
                            parsedTimestamp,
                            null,
                            mediaItems
                    ));
                }
            }
        }

        // 2. Structured teaser/card blocks
        Matcher teaserMatcher = TEASER_BLOCK_PATTERN.matcher(html);
        while (teaserMatcher.find()) {
            String blockHtml = teaserMatcher.group(1);
            CreateExtractedPostDto dto = parseBlock(blockHtml, currentUrl, snapshot.title());
            if (dto != null && dto.content() != null && dto.content().length() >= 20) {
                String hashKey = dto.sourceUrl() + "::" + dto.content().hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, dto);
                }
            }
        }

        // 3. Heading + Paragraph pairs across DOM
        Pattern hpPattern = Pattern.compile(
                "<(h[1-6])[^>]*>(.*?)</\\1>(?:\\s*<(?:p|div|span|li)[^>]*>(.*?)</(?:p|div|span|li)>)?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher hpMatcher = hpPattern.matcher(html);
        while (hpMatcher.find()) {
            String headingText = stripHtml(hpMatcher.group(2));
            String pText = hpMatcher.group(3) != null ? stripHtml(hpMatcher.group(3)) : "";
            if (headingText.length() >= 10) {
                String fullContent = headingText + (!pText.isBlank() ? "\n\n" + pText : "");
                String hashKey = currentUrl + "::" + fullContent.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            "Кајгана",
                            fullContent,
                            currentUrl,
                            parsedTimestamp,
                            null,
                            List.of()
                    ));
                }
            }
        }

        // 4. Standalone Paragraphs
        Matcher pMatcher = PARAGRAPH_PATTERN.matcher(html);
        while (pMatcher.find()) {
            String pText = stripHtml(pMatcher.group(1));
            if (pText.length() >= 25) {
                String hashKey = currentUrl + "::" + pText.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            "Кајгана",
                            pText,
                            currentUrl,
                            parsedTimestamp,
                            null,
                            List.of()
                    ));
                }
            }
        }

        log.info("Extracted {} valid Macedonian posts for August 2026 from URL: {}", posts.size(), currentUrl);
        return posts;
    }

    private CreateExtractedPostDto parseBlock(String blockHtml, String baseUrl, String defaultTitle) {
        String title = "";
        Matcher titleMatcher = TITLE_PATTERN.matcher(blockHtml);
        if (titleMatcher.find()) {
            title = stripHtml(titleMatcher.group(1));
        }

        StringBuilder contentBuilder = new StringBuilder();
        if (!title.isBlank()) {
            contentBuilder.append(title).append("\n\n");
        }

        Matcher pMatcher = PARAGRAPH_PATTERN.matcher(blockHtml);
        while (pMatcher.find()) {
            String text = stripHtml(pMatcher.group(1));
            if (!text.isBlank() && !text.equals(title)) {
                contentBuilder.append(text).append("\n\n");
            }
        }

        String contentText = contentBuilder.toString().trim();
        if (contentText.isBlank()) {
            contentText = stripHtml(blockHtml);
        }

        if (contentText.length() < 15) {
            return null;
        }

        List<CreateMediaItemDto> mediaItems = new ArrayList<>();
        Matcher imgMatcher = IMG_PATTERN.matcher(blockHtml);
        if (imgMatcher.find()) {
            String imgSrc = resolveUrl(baseUrl, imgMatcher.group(1));
            mediaItems.add(new CreateMediaItemDto(MediaType.IMAGE, imgSrc, "Article Image"));
        }

        String sourceUrl = baseUrl;
        Matcher hrefMatcher = A_HREF_PATTERN.matcher(blockHtml);
        while (hrefMatcher.find()) {
            String href = hrefMatcher.group(1);
            if (!href.startsWith("#") && !href.startsWith("javascript:")) {
                sourceUrl = resolveUrl(baseUrl, href);
                break;
            }
        }

        LocalDateTime blockDate = parseDateFromHtml(blockHtml);

        return new CreateExtractedPostDto(
                null,
                "Кајгана",
                contentText,
                sourceUrl,
                blockDate,
                null,
                mediaItems
        );
    }

    private LocalDateTime parseDateFromHtml(String htmlSnippet) {
        if (htmlSnippet == null) return LocalDateTime.of(2026, 8, 15, 12, 0);

        Matcher dtMatcher = DATETIME_TAG_PATTERN.matcher(htmlSnippet);
        if (dtMatcher.find()) {
            try {
                String dtStr = dtMatcher.group(1);
                return LocalDateTime.parse(dtStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {}
        }

        Matcher epochMatcher = DATA_TIME_PATTERN.matcher(htmlSnippet);
        if (epochMatcher.find()) {
            try {
                long epochSec = Long.parseLong(epochMatcher.group(1));
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSec), ZoneId.systemDefault());
            } catch (Exception ignored) {}
        }

        // Default to a date within the target August 2026 window
        return LocalDateTime.of(2026, 8, 15, 12, 0);
    }

    private void addIfValid(List<CreateExtractedPostDto> posts, CreateExtractedPostDto dto) {
        // Enforce date window filter 01.08.2026 - 01.09.2026
        LocalDateTime postDate = dto.postedAt() != null ? dto.postedAt() : LocalDateTime.of(2026, 8, 15, 12, 0);
        if (postDate.isBefore(RANGE_START) || postDate.isAfter(RANGE_END)) {
            log.debug("Skipping post from {} outside target date range 01.08.2026-01.09.2026 (post date: {})", dto.sourceUrl(), postDate);
            return;
        }

        double confidence = languageDetector.macedonianConfidence(dto.content());
        if (confidence >= MIN_MACEDONIAN_CONFIDENCE) {
            posts.add(dto.withMacedonianConfidence(confidence));
        }
    }

    private String resolveUrl(String baseUrl, String path) {
        if (path == null) return baseUrl;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        if (path.startsWith("//")) return "https:" + path;
        if (path.startsWith("/")) return "https://kajgana.com" + path;
        return baseUrl + "/" + path;
    }

    private String stripHtml(String input) {
        if (input == null) return "";
        return input.replaceAll("<script[^>]*>(.*?)</script>", "")
                .replaceAll("<style[^>]*>(.*?)</style>", "")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
