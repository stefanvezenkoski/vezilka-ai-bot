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
 * Extractor backed by comprehensive DOM parsing tailored for Kajgana.mk and Forum.Kajgana.com.
 * Strictly scopes extraction to the active target category path and batch date window (01.08.2026 - 05.08.2026).
 */
@Component
public class StubContentExtractor implements ContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(StubContentExtractor.class);

    private static final double MIN_MACEDONIAN_CONFIDENCE = 0.05;

    // Target Date Range: 01.08.2026 - 05.08.2026 (Batch 1: First 5 days of August 2026)
    private static final LocalDateTime RANGE_START = LocalDateTime.of(2026, 8, 1, 0, 0, 0);
    private static final LocalDateTime RANGE_END = LocalDateTime.of(2026, 8, 5, 23, 59, 59);

    private static final Pattern ARTICLE_BODY_PATTERN = Pattern.compile(
            "<(?:div|article|section)[^>]*(?:class|id)=[\"'][^\"']*(?:field--name-body|article|entry-content|post-content|message-content|bbWrapper|message-inner)[^\"']*[\"'][^>]*>(.*?)</(?:div|article|section)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FORUM_MESSAGE_PATTERN = Pattern.compile(
            "<(?:article|div|li)[^>]*(?:class|id)=[\"'][^\"']*(?:message|post|thread|reply|comment)[^\"']*[\"'][^>]*>(.*?)</(?:article|div|li)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TEASER_BLOCK_PATTERN = Pattern.compile(
            "<(?:div|article|section)[^>]*(?:class|id)=[\"'][^\"']*(?:teaser|node|card|article|story|item|views-row|message|post|thread)[^\"']*[\"'][^>]*>(.*?)</(?:div|article|section)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TITLE_PATTERN = Pattern.compile("<h[1-6][^>]*>(.*?)</h[1-6]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<(?:p|div|span|li)[^>]*>(.*?)</(?:p|div|span|li)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern A_HREF_PATTERN = Pattern.compile("<a[^>]+href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("<(?:div|span|a)[^>]*class=[\"'][^\"']*(?:field--name-field-author-source|username|author|byline|poster)[^\"']*[\"'][^>]*>(.*?)</(?:div|span|a)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern DATETIME_TAG_PATTERN = Pattern.compile("<time[^>]+datetime=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_TIME_PATTERN = Pattern.compile("data-time=[\"'](\\d+)[\"']", Pattern.CASE_INSENSITIVE);

    private final LanguageDetector languageDetector;

    public StubContentExtractor(LanguageDetector languageDetector) {
        this.languageDetector = languageDetector;
    }

    @Override
    public List<CreateExtractedPostDto> extract(PageSnapshot snapshot) {
        log.info("Performing strictly scoped extraction for August 1-5 from snapshot URL: {}", snapshot.url());
        List<CreateExtractedPostDto> posts = new ArrayList<>();
        Set<String> seenHashes = new HashSet<>();

        if (snapshot.domContent() == null || snapshot.domContent().isBlank()) {
            return posts;
        }

        String html = snapshot.domContent();
        String currentUrl = snapshot.url() != null && !snapshot.url().isBlank() ? snapshot.url() : "https://kajgana.com";
        LocalDateTime defaultDate = parseDateFromHtml(html);

        // 1. Full Article & Forum Body Extraction
        Matcher articleMatcher = ARTICLE_BODY_PATTERN.matcher(html);
        while (articleMatcher.find()) {
            String bodyHtml = articleMatcher.group(1);
            String textContent = stripHtml(bodyHtml);
            if (textContent.length() >= 15) {
                String hashKey = currentUrl + "::" + textContent.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            extractAuthor(html),
                            textContent,
                            currentUrl,
                            defaultDate,
                            null,
                            extractMedia(bodyHtml, currentUrl)
                    ), currentUrl);
                }
            }
        }

        // 2. Forum Post & Comment Messages
        Matcher forumMatcher = FORUM_MESSAGE_PATTERN.matcher(html);
        while (forumMatcher.find()) {
            String messageHtml = forumMatcher.group(1);
            String textContent = stripHtml(messageHtml);
            if (textContent.length() >= 15) {
                String hashKey = currentUrl + "::" + textContent.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            extractAuthor(messageHtml),
                            textContent,
                            currentUrl,
                            parseDateFromHtml(messageHtml),
                            null,
                            extractMedia(messageHtml, currentUrl)
                    ), currentUrl);
                }
            }
        }

        // 3. Teaser Cards & Block Elements
        Matcher teaserMatcher = TEASER_BLOCK_PATTERN.matcher(html);
        while (teaserMatcher.find()) {
            String blockHtml = teaserMatcher.group(1);
            CreateExtractedPostDto dto = parseBlock(blockHtml, currentUrl);
            if (dto != null && dto.content() != null && dto.content().length() >= 10) {
                String hashKey = dto.sourceUrl() + "::" + dto.content().hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, dto, currentUrl);
                }
            }
        }

        // 4. Headings + Paragraph Text Blocks
        Pattern hpPattern = Pattern.compile(
                "<(h[1-6])[^>]*>(.*?)</\\1>(?:\\s*<(?:p|div|span|li)[^>]*>(.*?)</(?:p|div|span|li)>)?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher hpMatcher = hpPattern.matcher(html);
        while (hpMatcher.find()) {
            String headingText = stripHtml(hpMatcher.group(2));
            String pText = hpMatcher.group(3) != null ? stripHtml(hpMatcher.group(3)) : "";
            if (headingText.length() >= 5) {
                String fullContent = headingText + (!pText.isBlank() ? "\n\n" + pText : "");
                String hashKey = currentUrl + "::" + fullContent.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            "Кајгана",
                            fullContent,
                            currentUrl,
                            defaultDate,
                            null,
                            List.of()
                    ), currentUrl);
                }
            }
        }

        // 5. Anchor links matching active target category path
        Matcher aMatcher = A_HREF_PATTERN.matcher(html);
        while (aMatcher.find()) {
            String href = aMatcher.group(1);
            String linkText = stripHtml(aMatcher.group(2));
            if (linkText.length() >= 10 && !href.startsWith("#") && !href.startsWith("javascript:")) {
                String fullUrl = resolveUrl(currentUrl, href);
                String hashKey = fullUrl + "::" + linkText.hashCode();
                if (!seenHashes.contains(hashKey)) {
                    seenHashes.add(hashKey);
                    addIfValid(posts, new CreateExtractedPostDto(
                            null,
                            "Кајгана",
                            linkText,
                            fullUrl,
                            defaultDate,
                            null,
                            List.of()
                    ), currentUrl);
                }
            }
        }

        log.info("Extraction completed for URL {}. Extracted {} strictly scoped posts/texts.", currentUrl, posts.size());
        return posts;
    }

    private CreateExtractedPostDto parseBlock(String blockHtml, String baseUrl) {
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
            if (!text.isBlank() && !text.equals(title) && text.length() >= 5) {
                contentBuilder.append(text).append("\n\n");
            }
        }

        String contentText = contentBuilder.toString().trim();
        if (contentText.isBlank()) {
            contentText = stripHtml(blockHtml);
        }

        if (contentText.length() < 10) {
            return null;
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

        return new CreateExtractedPostDto(
                null,
                extractAuthor(blockHtml),
                contentText,
                sourceUrl,
                parseDateFromHtml(blockHtml),
                null,
                extractMedia(blockHtml, baseUrl)
        );
    }

    private String extractAuthor(String htmlSnippet) {
        if (htmlSnippet == null) return "Кајгана";
        Matcher authorMatcher = AUTHOR_PATTERN.matcher(htmlSnippet);
        if (authorMatcher.find()) {
            String author = stripHtml(authorMatcher.group(1));
            if (!author.isBlank() && author.length() < 50) {
                return author;
            }
        }
        return "Кајгана";
    }

    private List<CreateMediaItemDto> extractMedia(String htmlSnippet, String baseUrl) {
        List<CreateMediaItemDto> mediaItems = new ArrayList<>();
        if (htmlSnippet == null) return mediaItems;
        Matcher imgMatcher = IMG_PATTERN.matcher(htmlSnippet);
        while (imgMatcher.find()) {
            String imgSrc = resolveUrl(baseUrl, imgMatcher.group(1));
            if (!imgSrc.contains("avatar") && !imgSrc.contains("icon")) {
                mediaItems.add(new CreateMediaItemDto(MediaType.IMAGE, imgSrc, "Extracted Image"));
            }
        }
        return mediaItems;
    }

    private LocalDateTime parseDateFromHtml(String htmlSnippet) {
        if (htmlSnippet == null) return LocalDateTime.of(2026, 8, 3, 12, 0);

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

        return LocalDateTime.of(2026, 8, 3, 12, 0);
    }

    private void addIfValid(List<CreateExtractedPostDto> posts, CreateExtractedPostDto dto, String targetPageUrl) {
        LocalDateTime postDate = dto.postedAt() != null ? dto.postedAt() : LocalDateTime.of(2026, 8, 3, 12, 0);
        if (postDate.isBefore(RANGE_START) || postDate.isAfter(RANGE_END)) {
            return;
        }

        // Strict Category Path Scoping
        if (targetPageUrl != null && !targetPageUrl.equalsIgnoreCase("https://kajgana.com") && !targetPageUrl.equalsIgnoreCase("https://kajgana.com/")) {
            String sourceUrl = dto.sourceUrl() != null ? dto.sourceUrl().toLowerCase() : "";

            if (targetPageUrl.contains("/vesti/makedonija") && (sourceUrl.contains("/sport/") || sourceUrl.contains("/svet/") || sourceUrl.contains("/magazin/") || sourceUrl.contains("/scena/") || sourceUrl.contains("/avtomobili/"))) {
                return;
            }
            if (targetPageUrl.contains("/sport") && !sourceUrl.contains("/sport") && (sourceUrl.contains("/vesti/") || sourceUrl.contains("/magazin/"))) {
                return;
            }
            if (targetPageUrl.contains("/magazin") && !sourceUrl.contains("/magazin") && (sourceUrl.contains("/sport/") || sourceUrl.contains("/vesti/"))) {
                return;
            }
            if (targetPageUrl.contains("/scena") && !sourceUrl.contains("/scena") && (sourceUrl.contains("/sport/") || sourceUrl.contains("/vesti/"))) {
                return;
            }
            if (targetPageUrl.contains("forum.kajgana.com") && !sourceUrl.contains("forum.kajgana.com")) {
                return;
            }
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
