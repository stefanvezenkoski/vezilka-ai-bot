package mk.ukim.finki.aibotbackend.bot.llm;

import java.util.List;
import java.util.Map;
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
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_gemini_api_key")) {
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
        log.info("Deciding next action with Gemini LLM for goal: '{}'. Step history size: {}", goal, history.size());

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_gemini_api_key")) {
            log.warn("Gemini API key is not configured. Executing deep multi-pass mock automation sequence.");

            String targetUrl = snapshot != null && snapshot.url() != null && !snapshot.url().isBlank()
                    ? snapshot.url()
                    : "https://kajgana.com";

            int stepCount = history.size();
            switch (stepCount) {
                case 0:
                    return new BotDecision(
                        new BotAction(BotActionType.NAVIGATE, targetUrl, null, "Initial navigation to category page"),
                        false,
                        "Step 1: Navigating to category target " + targetUrl
                    );
                case 1:
                    return new BotDecision(
                        new BotAction(BotActionType.SCROLL, null, null, "First scroll down to load article stream"),
                        false,
                        "Step 2: Scrolling down category feed"
                    );
                case 2:
                    return new BotDecision(
                        new BotAction(BotActionType.EXTRACT, null, null, "Extract first wave of category articles"),
                        false,
                        "Step 3: Extracting first batch of articles"
                    );
                case 3:
                    return new BotDecision(
                        new BotAction(BotActionType.SCROLL, null, null, "Second scroll down to trigger lazy loading"),
                        false,
                        "Step 4: Deep scrolling down category feed"
                    );
                case 4:
                    return new BotDecision(
                        new BotAction(BotActionType.EXTRACT, null, null, "Extract second wave of category articles"),
                        false,
                        "Step 5: Extracting second batch of articles"
                    );
                case 5:
                    return new BotDecision(
                        new BotAction(BotActionType.SCROLL, null, null, "Third scroll down to reach older monthly articles"),
                        false,
                        "Step 6: Scrolling to bottom of feed"
                    );
                case 6:
                    return new BotDecision(
                        new BotAction(BotActionType.EXTRACT, null, null, "Extract final wave of category articles"),
                        false,
                        "Step 7: Extracting final batch of articles"
                    );
                default:
                    return new BotDecision(
                        new BotAction(BotActionType.FINISH, null, null, "Category extraction completed"),
                        true,
                        "Step 8: Category extraction finished successfully"
                    );
            }
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
            log.error("Failed to query Gemini API, falling back to mock decision", e);
            return new BotDecision(
                new BotAction(BotActionType.FINISH, null, null, "Finish due to API error: " + e.getMessage()),
                true,
                "API request failed, finishing gracefully."
            );
        }
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
