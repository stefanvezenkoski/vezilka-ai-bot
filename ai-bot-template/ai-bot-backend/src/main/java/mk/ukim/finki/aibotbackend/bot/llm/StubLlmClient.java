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
    private static final String GEMINI_MODEL = "gemini-3.6-flash";
    private static final String GEMINI_REQUEST_FAILED = "Gemini request failed";
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s'\\\"]+");

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
                    .uri("/v1beta/models/" + GEMINI_MODEL + ":generateContent?key={key}", apiKey)
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
            log.warn("Gemini API key is not configured. Executing multi-step mock automation sequence.");

            return fallbackDecision(snapshot, goal, history, "Gemini API key is not configured");
        }

        if (history.stream().anyMatch(action -> action.reasoning() != null
                && action.reasoning().startsWith(GEMINI_REQUEST_FAILED))) {
            log.warn("Skipping further Gemini requests after an API failure; completing deterministic extraction.");
            return fallbackDecision(snapshot, goal, history, GEMINI_REQUEST_FAILED);
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

            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/" + GEMINI_MODEL + ":generateContent?key={key}", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseGeminiResponse(response, snapshot, goal, history);
        } catch (Exception e) {
            log.error("Failed to query Gemini API; continuing with deterministic extraction", e);
            return fallbackDecision(snapshot, goal, history, GEMINI_REQUEST_FAILED);
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
        sb.append("You must choose EXTRACT after reaching relevant content. Set goalReached to true only after EXTRACT has already been performed.\n");
        sb.append("Return valid JSON with keys: 'action' (with 'type', 'target', 'value', 'reasoning'), 'goalReached' (boolean), 'rationale' (string).\n");

        return sb.toString();
    }

    private String truncateHtml(String html) {
        if (html == null) return "";
        return html.length() > 3000 ? html.substring(0, 3000) + "... [truncated]" : html;
    }

    private BotDecision parseGeminiResponse(
        Map<?, ?> response,
        PageSnapshot snapshot,
        String goal,
        List<BotAction> history
    ) {
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
            log.error("Failed to parse Gemini response; continuing with deterministic extraction", e);
            return fallbackDecision(snapshot, goal, history, "Invalid Gemini response");
        }
    }

    /**
     * Keeps extraction operational when the optional LLM service is unavailable.
     * Continuously navigates to unvisited internal links discovered in the DOM.
     */
    private BotDecision fallbackDecision(
        PageSnapshot snapshot,
        String goal,
        List<BotAction> history,
        String reason
    ) {
        String targetUrl = targetUrl(goal, snapshot);
        int step = history.size();

        if (step == 0) {
            return new BotDecision(
                new BotAction(BotActionType.NAVIGATE, targetUrl, null, reason + ": navigate to extraction target"),
                false,
                "Opening initial extraction target."
            );
        }

        BotAction lastAction = history.get(step - 1);
        if (lastAction.type() == BotActionType.NAVIGATE) {
            return new BotDecision(
                new BotAction(BotActionType.WAIT, null, null, "Wait for page content to load"),
                false,
                "Waiting for current page content to load."
            );
        }

        if (lastAction.type() == BotActionType.WAIT) {
            return new BotDecision(
                new BotAction(BotActionType.SCROLL, null, null, "Scroll down to reveal lazy content & list items"),
                false,
                "Scrolling down to expose lazy elements."
            );
        }

        if (lastAction.type() == BotActionType.SCROLL) {
            return new BotDecision(
                new BotAction(BotActionType.EXTRACT, null, null, "Extract Macedonian content from DOM"),
                false,
                "Extracting Macedonian DOM content from current page."
            );
        }

        // After EXTRACT, pick an unvisited internal link discovered in DOM
        if (lastAction.type() == BotActionType.EXTRACT) {
            List<String> unvisited = discoverUnvisitedLinks(snapshot, history);
            if (!unvisited.isEmpty()) {
                String nextUrl = unvisited.get(0);
                log.info("Crawling next unvisited internal link ({} of {} candidates remaining): {}", unvisited.size(), unvisited.size(), nextUrl);
                return new BotDecision(
                    new BotAction(BotActionType.NAVIGATE, nextUrl, null, "Crawl next internal link: " + nextUrl),
                    false,
                    "Navigating to next unvisited subpage."
                );
            }
        }

        // If no unvisited link left, attempt another SCROLL or EXTRACT before finishing
        if (step < 48) {
            return new BotDecision(
                new BotAction(BotActionType.SCROLL, null, null, "Scroll to uncover bottom links"),
                false,
                "Scrolling to find additional content."
            );
        }

        return new BotDecision(
            new BotAction(BotActionType.FINISH, null, null, "Multi-page extraction complete"),
            true,
            "Autonomous multi-page crawling sequence finished."
        );
    }

    private List<String> discoverUnvisitedLinks(PageSnapshot snapshot, List<BotAction> history) {
        if (snapshot == null || snapshot.domContent() == null || snapshot.domContent().isBlank()) {
            return java.util.List.of();
        }

        java.util.Set<String> visited = new java.util.HashSet<>();
        for (BotAction action : history) {
            if (action.type() == BotActionType.NAVIGATE && action.target() != null) {
                visited.add(normalizeUrl(action.target()));
            }
        }
        if (snapshot.url() != null) {
            visited.add(normalizeUrl(snapshot.url()));
        }

        List<String> articleLinks = new java.util.ArrayList<>();
        List<String> categoryLinks = new java.util.ArrayList<>();

        Matcher matcher = Pattern.compile("<a[^>]+href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(snapshot.domContent());
        while (matcher.find()) {
            String href = matcher.group(1).trim();
            
            // Reject empty, fragment, script, file, or meta links
            if (href.startsWith("#") || href.startsWith("javascript:") || href.contains("mailto:") 
                    || href.endsWith(".png") || href.endsWith(".jpg") || href.endsWith(".jpeg") 
                    || href.endsWith(".gif") || href.endsWith(".pdf") || href.endsWith(".css") || href.endsWith(".js")
                    || href.contains("/user/") || href.contains("/impresum") || href.contains("/rss") || href.contains("/marketing")) {
                continue;
            }

            // Reject external social media and third-party domains strictly
            String lowerHref = href.toLowerCase();
            if (lowerHref.contains("facebook.com") || lowerHref.contains("instagram.com") 
                    || lowerHref.contains("twitter.com") || lowerHref.contains("youtube.com") 
                    || lowerHref.contains("google.com") || lowerHref.contains("dotmetrics") 
                    || lowerHref.contains("googletagmanager")) {
                continue;
            }

            String fullUrl = resolveUrl("https://kajgana.com", href);
            String normalized = normalizeUrl(fullUrl);

            // Enforce domain boundary: MUST be kajgana.com or forum.kajgana.com
            boolean isInternalDomain = fullUrl.startsWith("https://kajgana.com") 
                    || fullUrl.startsWith("http://kajgana.com") 
                    || fullUrl.startsWith("https://forum.kajgana.com") 
                    || fullUrl.startsWith("http://forum.kajgana.com");

            if (isInternalDomain && !visited.contains(normalized) 
                    && !articleLinks.contains(fullUrl) 
                    && !categoryLinks.contains(fullUrl)) {
                
                String path = fullUrl.replace("https://kajgana.com", "").replace("http://kajgana.com", "")
                        .replace("https://forum.kajgana.com", "").replace("http://forum.kajgana.com", "");
                
                String[] segments = path.split("/");
                int validSegments = 0;
                for (String seg : segments) {
                    if (!seg.isBlank()) validSegments++;
                }

                if (validSegments >= 3 || path.contains("-")) {
                    articleLinks.add(fullUrl);
                } else if (validSegments >= 1) {
                    categoryLinks.add(fullUrl);
                }
            }
        }

        // Return internal news article links first, followed by internal category links
        List<String> combined = new java.util.ArrayList<>(articleLinks);
        combined.addAll(categoryLinks);
        return combined;
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("/$", "").toLowerCase();
    }

    private String resolveUrl(String baseUrl, String path) {
        if (path == null) return baseUrl;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        if (path.startsWith("//")) return "https:" + path;
        if (path.startsWith("/")) return "https://kajgana.com" + path;
        return baseUrl + "/" + path;
    }

    private String targetUrl(String goal, PageSnapshot snapshot) {
        if (goal != null) {
            Matcher matcher = URL_PATTERN.matcher(goal);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        if (snapshot != null && snapshot.url() != null && !snapshot.url().isBlank()) {
            return snapshot.url();
        }
        return "https://kajgana.com";
    }
}
