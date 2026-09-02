package mk.ukim.finki.aibotbackend.bot.llm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("Deciding next action via Google Gemini AI for goal: '{}'. Step history size: {}", goal, history.size());

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

        if (apiKey == null || apiKey.isBlank() || isPlaceholderKey(apiKey)) {
            log.info("Gemini API key is not active. Using automated sequence.");
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
            log.error("Failed to query Gemini API; continuing with deterministic extraction", e);
            return decideMockNextAction(goal, history);
        }
    }

    private BotDecision decideMockNextAction(String goal, List<BotAction> history) {
        String targetUrl = extractUrlFromGoal(goal);
        int stepCount = history.size();

        log.info("Automated crawler sequence step {} for target URL: {}", stepCount, targetUrl);

        int cycle = stepCount % 3;
        int pageIndex = stepCount / 3;

        if (pageIndex >= 8) {
            return new BotDecision(
                new BotAction(BotActionType.FINISH, null, null, "Massive multi-page extraction completed successfully"),
                true,
                "Finished deep crawling across 8 pages of target category"
            );
        }

        switch (cycle) {
            case 0:
                String pagedUrl = buildPagedUrl(targetUrl, pageIndex);
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, pagedUrl, null, "Navigating to feed page " + pageIndex),
                    false,
                    "Step " + (stepCount + 1) + ": Opening page " + pageIndex + " of category feed: " + pagedUrl
                );
            case 1:
                return new BotDecision(
                    new BotAction(BotActionType.SCROLL, null, null, "Scrolling down page " + pageIndex),
                    false,
                    "Step " + (stepCount + 1) + ": Scrolling down to load lazy article elements on page " + pageIndex
                );
            case 2:
            default:
                return new BotDecision(
                    new BotAction(BotActionType.EXTRACT, null, null, "Deep extracting all full articles from page " + pageIndex),
                    false,
                    "Step " + (stepCount + 1) + ": Extracting all full articles from page " + pageIndex
                );
        }
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
        sb.append("- You are crawling news articles and forum threads to collect a LARGE quantity of texts from 1-5 August 2026.\n");
        sb.append("- You MUST cycle through pagination: ?page=0, ?page=1, ?page=2, ?page=3, ?page=4, ?page=5, etc.\n");
        sb.append("- On each page, perform SCROLL and EXTRACT actions to deep-extract all articles.\n");
        sb.append("- Do NOT set goalReached: true until at least 6-8 pages have been visited and extracted.\n");
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

            // Clean json backticks if model wraps markdown
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
