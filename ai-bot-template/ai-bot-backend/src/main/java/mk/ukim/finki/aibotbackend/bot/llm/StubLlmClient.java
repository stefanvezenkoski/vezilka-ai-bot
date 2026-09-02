package mk.ukim.finki.aibotbackend.bot.llm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import mk.ukim.finki.aibotbackend.model.enums.BotActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StubLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(StubLlmClient.class);
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s]+");
    private static final Pattern ARTICLE_HREF_PATTERN = Pattern.compile("<a[^>]+href=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);

    @Value("${bot.llm-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public StubLlmClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        log.info("Low-level LLM completion requested");
        if (apiKey == null || apiKey.isBlank() || isPlaceholderKey(apiKey)) {
            return "Mock completion response";
        }
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", systemPrompt + "\n\n" + userPrompt)
                    ))
                )
            );

            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/gemini-3.6-flash:generateContent?key={key}", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            log.error("Failed to call Gemini API in complete()", e);
            return "Fallback completion response: " + e.getMessage();
        }
    }

    @Override
    public BotDecision decideNextAction(PageSnapshot snapshot, String goal, List<BotAction> history) {
        log.info("Deciding next action for goal: '{}'. Step history size: {}", goal, history.size());

        String targetUrl = extractUrlFromGoal(goal);

        // Safeguard for Step 0: Ensure initial navigation happens first
        if (history.isEmpty() || snapshot == null || snapshot.url() == null || snapshot.url().contains("about:blank")) {
            log.info("Initial navigation safeguard triggered for target URL: {}", targetUrl);
            return new BotDecision(
                new BotAction(BotActionType.NAVIGATE, targetUrl, null, "Navigating to target category URL " + targetUrl),
                false,
                "Step 1: Initial navigation to target category feed"
            );
        }

        // If on an individual article page, extract content and prepare to navigate to next
        String currentUrl = snapshot.url();
        boolean isArticlePage = isIndividualArticleUrl(currentUrl, targetUrl);

        if (isArticlePage) {
            BotAction lastAction = !history.isEmpty() ? history.get(history.size() - 1) : null;
            if (lastAction != null && lastAction.type() == BotActionType.NAVIGATE) {
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Reading and extracting full content of article: " + currentUrl),
                    false,
                    "Extracting complete text directly from article page: " + currentUrl
                );
            }
        }

        if (apiKey == null || apiKey.isBlank() || isPlaceholderKey(apiKey)) {
            log.info("Gemini API key is not active. Using deep link crawler sequence.");
            return decideMockNextAction(snapshot, goal, history);
        }

        try {
            String promptText = buildPrompt(snapshot, goal, history);

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", promptText)))
                ),
                "generationConfig", Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                            "action", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                    "type", Map.of("type", "STRING"),
                                    "target", Map.of("type", "STRING"),
                                    "value", Map.of("type", "STRING"),
                                    "reasoning", Map.of("type", "STRING")
                                ),
                                "required", List.of("type")
                            ),
                            "goalReached", Map.of("type", "BOOLEAN"),
                            "rationale", Map.of("type", "STRING")
                        ),
                        "required", List.of("action", "goalReached", "rationale")
                    )
                )
            );

            log.info("Querying Google Gemini 3.6 Flash AI endpoint...");
            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/gemini-3.6-flash:generateContent?key={key}", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            BotDecision decision = parseGeminiResponse(response);
            log.info("Gemini AI Decision: action={}, goalReached={}, rationale='{}'", 
                    decision.action().type(), decision.goalReached(), decision.rationale());
            return decision;
        } catch (Exception e) {
            log.error("Failed to query Gemini API; continuing with deep link crawler sequence", e);
            return decideMockNextAction(snapshot, goal, history);
        }
    }

    private BotDecision decideMockNextAction(PageSnapshot snapshot, String goal, List<BotAction> history) {
        String targetUrl = extractUrlFromGoal(goal);
        String currentUrl = snapshot != null && snapshot.url() != null ? snapshot.url() : targetUrl;

        Set<String> visitedUrls = new HashSet<>();
        for (BotAction action : history) {
            if (action.type() == BotActionType.NAVIGATE && action.target() != null) {
                visitedUrls.add(action.target());
            }
        }

        // If on article page and just extracted, return to feed or visit next article
        if (isIndividualArticleUrl(currentUrl, targetUrl)) {
            // Find next unvisited article from history or go to next feed page
            List<String> discovered = extractArticleLinks(snapshot != null ? snapshot.domContent() : "", targetUrl);
            for (String link : discovered) {
                if (!visitedUrls.contains(link)) {
                    return new BotDecision(
                        new BotAction(BotActionType.NAVIGATE, link, null, "Visiting next news article: " + link),
                        false,
                        "Navigating into news article to read full body text: " + link
                    );
                }
            }
            // If all discovered articles on this screen are visited, advance feed page
            int currentPageNum = extractPageNumber(currentUrl);
            String nextFeedPage = buildPagedUrl(targetUrl, currentPageNum + 1);
            if (currentPageNum < 6) {
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, nextFeedPage, null, "Returning to category feed page " + (currentPageNum + 1)),
                    false,
                    "All articles visited on current page; advancing to feed page " + (currentPageNum + 1)
                );
            } else {
                return new BotDecision(
                    new BotAction(BotActionType.FINISH, null, null, "All pages and articles successfully scraped"),
                    true,
                    "Completed deep link extraction across multiple pages"
                );
            }
        }

        // We are on a category listing / feed page
        List<String> articleLinks = extractArticleLinks(snapshot != null ? snapshot.domContent() : "", targetUrl);
        for (String link : articleLinks) {
            if (!visitedUrls.contains(link)) {
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, link, null, "Visiting news article: " + link),
                    false,
                    "Navigating into news article to read full body text: " + link
                );
            }
        }

        // All links on this feed page visited; advance to next page
        int pageNum = extractPageNumber(currentUrl);
        if (pageNum < 6) {
            String nextPage = buildPagedUrl(targetUrl, pageNum + 1);
            return new BotDecision(
                new BotAction(BotActionType.NAVIGATE, nextPage, null, "Advancing to category feed page " + (pageNum + 1)),
                false,
                "All articles visited on page " + pageNum + "; moving to page " + (pageNum + 1)
            );
        }

        return new BotDecision(
            new BotAction(BotActionType.FINISH, null, null, "Completed multi-page article crawling"),
            true,
            "Finished deep crawling all articles across all pages"
        );
    }

    private boolean isIndividualArticleUrl(String url, String baseCategoryUrl) {
        if (url == null) return false;
        String cleanUrl = url.replaceAll("\\?page=\\d+", "").replaceAll("&page=\\d+", "");
        String cleanBase = baseCategoryUrl.replaceAll("\\?page=\\d+", "").replaceAll("&page=\\d+", "");

        if (cleanUrl.equalsIgnoreCase(cleanBase) || cleanUrl.equalsIgnoreCase(cleanBase + "/")) {
            return false;
        }
        if (cleanUrl.contains("page=") || cleanUrl.endsWith("/vesti") || cleanUrl.endsWith("/sport") || cleanUrl.endsWith("/magazin")) {
            return false;
        }
        // Contains subpath like /vesti/makedonija/naslov-statija
        return cleanUrl.length() > cleanBase.length() + 3;
    }

    private List<String> extractArticleLinks(String html, String targetUrl) {
        List<String> links = new ArrayList<>();
        if (html == null || html.isBlank()) return links;

        String pathKeyword = "/vesti/";
        if (targetUrl.contains("/sport")) pathKeyword = "/sport/";
        else if (targetUrl.contains("/magazin")) pathKeyword = "/magazin/";
        else if (targetUrl.contains("/scena")) pathKeyword = "/scena/";
        else if (targetUrl.contains("forum.kajgana.com")) pathKeyword = "/threads/";

        Matcher matcher = ARTICLE_HREF_PATTERN.matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1);
            if (href.contains(pathKeyword) && !href.contains("page=") && !href.contains("#") && href.length() > 15) {
                String fullUrl = href.startsWith("http") ? href : "https://kajgana.com" + (href.startsWith("/") ? href : "/" + href);
                if (!links.contains(fullUrl)) {
                    links.add(fullUrl);
                }
            }
        }
        return links;
    }

    private int extractPageNumber(String url) {
        if (url == null) return 0;
        Matcher m = Pattern.compile("page=(\\d+)").matcher(url);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private boolean isPlaceholderKey(String key) {
        if (key == null) return true;
        String lower = key.toLowerCase();
        return lower.contains("your_gemini_api_key") || lower.contains("твојот") || lower.contains("kljuc");
    }

    private String extractUrlFromGoal(String goal) {
        if (goal == null) return "https://kajgana.com";
        Matcher matcher = URL_PATTERN.matcher(goal);
        if (matcher.find()) {
            String url = matcher.group(0);
            return url.replaceAll("[.,;)]$", "");
        }
        return "https://kajgana.com";
    }

    private String buildPagedUrl(String baseUrl, int page) {
        if (baseUrl.contains("?page=")) {
            return baseUrl.replaceAll("\\?page=\\d+", "?page=" + page);
        }
        if (baseUrl.contains("&page=")) {
            return baseUrl.replaceAll("&page=\\d+", "&page=" + page);
        }
        return baseUrl.contains("?") ? baseUrl + "&page=" + page : baseUrl + "?page=" + page;
    }

    private String buildPrompt(PageSnapshot snapshot, String goal, List<BotAction> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an active AI Web Crawler Agent targeting Macedonian websites (Kajgana.mk, Forum Kajgana).\n");
        sb.append("Goal: ").append(goal).append("\n");
        sb.append("Target Date Window: August 1 to August 5, 2026. Extract a large volume of full news articles.\n\n");
        sb.append("Current Page URL: ").append(snapshot != null ? snapshot.url() : "unknown").append("\n");
        sb.append("Current Page DOM snippet:\n").append(truncateHtml(snapshot != null ? snapshot.domContent() : "")).append("\n\n");

        sb.append("Action History:\n");
        for (int i = 0; i < history.size(); i++) {
            BotAction a = history.get(i);
            sb.append(i + 1).append(". ").append(a.type()).append(" -> ").append(a.target()).append(" (Reason: ").append(a.reasoning()).append(")\n");
        }

        sb.append("\nCRITICAL INSTRUCTIONS:\n");
        sb.append("- You must navigate directly into each news article link on the page (NAVIGATE -> article URL).\n");
        sb.append("- Once on the article page, run EXTRACT to read the complete article body.\n");
        sb.append("- Then proceed to the next article or advance to ?page=1, ?page=2 etc.\n");
        sb.append("- Return valid JSON matching schema with 'action' (with 'type', 'target', 'value', 'reasoning'), 'goalReached' (boolean), 'rationale' (string).\n");

        return sb.toString();
    }

    private String truncateHtml(String html) {
        if (html == null) return "";
        return html.length() > 3000 ? html.substring(0, 3000) + "... [truncated]" : html;
    }

    private BotDecision parseGeminiResponse(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            String jsonText = (String) firstPart.get("text");

            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            return objectMapper.readValue(jsonText, BotDecision.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON response from Gemini API: {}", e.getMessage(), e);
            return new BotDecision(
                new BotAction(BotActionType.FINISH, null, null, "Parse error: " + e.getMessage()),
                false,
                "Failed to parse LLM decision"
            );
        }
    }
}
