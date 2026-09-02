package mk.ukim.finki.aibotbackend.bot.llm;

import java.util.List;
import java.util.Map;
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

    @Value("${bot.llm-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    private final RestClient restClient;

    public StubLlmClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
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
                    .uri("/v1beta/models/gemini-1.5-flash:generateContent?key={key}", apiKey)
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

        if (apiKey == null || apiKey.isBlank() || isPlaceholderKey(apiKey)) {
            return decideMockNextAction(goal, history);
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
                            "reasoning", Map.of("type", "STRING")
                        ),
                        "required", List.of("action", "goalReached", "reasoning")
                    )
                )
            );

            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/gemini-1.5-flash:generateContent?key={key}", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseGeminiResponse(response);
        } catch (Exception e) {
            log.warn("Gemini API request failed ({}). Continuing extraction with automated multi-page crawler...", e.getMessage());
            return decideMockNextAction(goal, history);
        }
    }

    private BotDecision decideMockNextAction(String goal, List<BotAction> history) {
        String targetUrl = extractUrlFromGoal(goal);
        int stepCount = history.size();

        log.info("Automated crawler sequence step {} for target URL: {}", stepCount, targetUrl);

        switch (stepCount) {
            case 0:
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, targetUrl, null, "Navigating to target URL " + targetUrl),
                    false,
                    "Step 1: Opening target category page"
                );
            case 1:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Extracting clean articles from main category page"),
                    false,
                    "Step 2: Extracting articles from page 1"
                );
            case 2:
                return new BotDecision(
                    new BotAction(BotActionType.SCROLL, null, null, "Scrolling down category page"),
                    false,
                    "Step 3: Scrolling down to reveal more articles"
                );
            case 3:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Extracting second batch of clean articles"),
                    false,
                    "Step 4: Extracting articles post-scroll"
                );
            case 4:
                String page1Url = buildPagedUrl(targetUrl, 1);
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, page1Url, null, "Navigating to feed page 1"),
                    false,
                    "Step 5: Opening page 1 of category feed: " + page1Url
                );
            case 5:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Extracting articles from feed page 1"),
                    false,
                    "Step 6: Extracting articles from page 1"
                );
            case 6:
                String page2Url = buildPagedUrl(targetUrl, 2);
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, page2Url, null, "Navigating to feed page 2"),
                    false,
                    "Step 7: Opening page 2 of category feed: " + page2Url
                );
            case 7:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Extracting articles from feed page 2"),
                    false,
                    "Step 8: Extracting articles from page 2"
                );
            case 8:
                String page3Url = buildPagedUrl(targetUrl, 3);
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, page3Url, null, "Navigating to feed page 3"),
                    false,
                    "Step 9: Opening page 3 of category feed: " + page3Url
                );
            case 9:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Extracting articles from feed page 3"),
                    false,
                    "Step 10: Extracting articles from page 3"
                );
            default:
                return new BotDecision(
                    new BotAction(BotActionType.FINISH, null, null, "Category feed extraction completed successfully"),
                    true,
                    "Finished multi-page extraction for target category"
                );
        }
    }

    private boolean isPlaceholderKey(String key) {
        if (key == null) return true;
        String lower = key.toLowerCase();
        return lower.contains("your_gemini_api_key") || lower.contains("твојот") || lower.contains("kljuc") || lower.contains("key");
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
        sb.append("You are an AI Web Extraction Agent targeting Macedonian websites (Kajgana.mk, Forum Kajgana).\n");
        sb.append("Goal: ").append(goal).append("\n\n");
        sb.append("Current Page URL: ").append(snapshot != null ? snapshot.url() : "unknown").append("\n");
        sb.append("Current Page DOM snippet:\n").append(truncateHtml(snapshot != null ? snapshot.domContent() : "")).append("\n\n");

        sb.append("Action History:\n");
        for (int i = 0; i < history.size(); i++) {
            BotAction a = history.get(i);
            sb.append(i + 1).append(". ").append(a.type()).append(" -> ").append(a.target()).append(" (Reason: ").append(a.reasoning()).append(")\n");
        }

        sb.append("\nDecide the next action to take to fulfill the goal. Available action types:\n");
        sb.append("NAVIGATE, CLICK, TYPE, SCROLL, WAIT, LOGIN, EXTRACT, FINISH.\n");
        sb.append("Return valid JSON with keys: 'action' (with 'type', 'target', 'value', 'reasoning'), 'goalReached' (boolean), 'reasoning' (string).\n");

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

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(jsonText, BotDecision.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON response from Gemini API", e);
            return new BotDecision(
                new BotAction(BotActionType.FINISH, null, null, "Parse error"),
                true,
                "Failed to parse LLM decision"
            );
        }
    }
}
