package mk.ukim.finki.aibotbackend.bot.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mk.ukim.finki.aibotbackend.bot.browser.PageSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StubLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(StubLlmClient.class);

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public StubLlmClient(
            @Value("${GEMINI_API_KEY:}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_gemini_api_key_here")) {
            log.warn("Gemini API key is not configured. Returning dummy mock completion.");
            return "Mock completion: Please configure GEMINI_API_KEY in your .env file.";
        }

        try {
            // Build Gemini request payload structure
            Map<String, Object> requestBody = new HashMap<>();
            
            // Contents
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentMap = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> partMap = new HashMap<>();
            partMap.put("text", userPrompt);
            parts.add(partMap);
            contentMap.put("parts", parts);
            contents.add(contentMap);
            requestBody.put("contents", contents);

            // System Instruction
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                Map<String, Object> systemInstruction = new HashMap<>();
                List<Map<String, Object>> sysParts = new ArrayList<>();
                Map<String, Object> sysPartMap = new HashMap<>();
                sysPartMap.put("text", systemPrompt);
                sysParts.add(sysPartMap);
                systemInstruction.put("parts", sysParts);
                requestBody.put("systemInstruction", systemInstruction);
            }

            log.info("Sending complete request to Gemini API...");
            String responseJson = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/gemini-1.5-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text");
            
            return textNode.asText();

        } catch (Exception e) {
            log.error("Failed to call Gemini API complete method", e);
            throw new RuntimeException("Error communicating with Gemini API", e);
        }
    }

    @Override
    public BotDecision decideNextAction(PageSnapshot snapshot, String goal, List<BotAction> history) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_gemini_api_key_here")) {
            log.warn("Gemini API key is not configured. Returning mock action to NAVIGATE to Kajgana.");
            // If API key is not configured, simulate navigating to Kajgana.mk to avoid immediate crash
            if (history.isEmpty()) {
                return new BotDecision(
                    new BotAction(mk.ukim.finki.aibotbackend.model.enums.BotActionType.NAVIGATE, "https://kajgana.com", null, "Navigate to Kajgana"),
                    false,
                    "API key missing. Navigating to start target."
                );
            } else {
                return new BotDecision(
                    new BotAction(mk.ukim.finki.aibotbackend.model.enums.BotActionType.FINISH, null, null, "Finish due to missing API key"),
                    true,
                    "Finished mock execution."
                );
            }
        }

        String systemPrompt = """
            You are the decision-making brain of an autonomous web-scraping AI bot.
            Your goal is to achieve the user's extraction goal on the target website.
            
            Analyze the current page title, URL, and HTML DOM snippet provided.
            Review the history of actions executed so far to prevent loops.
            
            Choose the next single BotAction. You MUST respond with a VALID JSON object adhering to the following structure:
            {
              "action": {
                "type": "NAVIGATE" | "CLICK" | "TYPE" | "SCROLL" | "WAIT" | "EXTRACT" | "LOGIN" | "FINISH",
                "target": "target selector (CSS, xpath, or element text description for CLICK/TYPE, or a URL for NAVIGATE, or null)",
                "value": "text value to type (only if action type is TYPE, otherwise null)",
                "reasoning": "short explanation of this specific action"
              },
              "goalReached": boolean (set to true if the goal is fully achieved and no more actions are needed),
              "rationale": "overall explanation of your current state and strategy"
            }
            
            Rules:
            1. Use 'NAVIGATE' to load the initial page if you are not on the correct website.
            2. Use 'CLICK' to click buttons, tabs, links, or navigation options. Prefer raw CSS selectors if visible.
            3. Use 'TYPE' to enter query terms in search boxes.
            4. Use 'SCROLL' to scroll down if you need to load more posts or content.
            5. Use 'EXTRACT' when you see posts or content relevant to the goal.
            6. Use 'FINISH' when you have collected all relevant content or reached the goal.
            7. Ensure you do not repeat the exact same failing action. Try a different path or selector if something fails.
            """;

        // Limit DOM content length to prevent token issues
        String dom = snapshot.domContent();
        if (dom != null && dom.length() > 30000) {
            dom = dom.substring(0, 30000) + "... [TRUNCATED]";
        }

        try {
            // Build detailed user prompt
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("goal", goal);
            userPayload.put("currentUrl", snapshot.url());
            userPayload.put("pageTitle", snapshot.title());
            userPayload.put("domContent", dom);
            
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (BotAction action : history) {
                Map<String, Object> actMap = new HashMap<>();
                actMap.put("type", action.type().name());
                actMap.put("target", action.target());
                actMap.put("value", action.value());
                actMap.put("reasoning", action.reasoning());
                historyList.add(actMap);
            }
            userPayload.put("actionHistory", historyList);

            String userPrompt = objectMapper.writeValueAsString(userPayload);

            // Construct Gemini payload
            Map<String, Object> requestBody = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentMap = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> partMap = new HashMap<>();
            partMap.put("text", userPrompt);
            parts.add(partMap);
            contentMap.put("parts", parts);
            contents.add(contentMap);
            requestBody.put("contents", contents);

            Map<String, Object> systemInstruction = new HashMap<>();
            List<Map<String, Object>> sysParts = new ArrayList<>();
            Map<String, Object> sysPartMap = new HashMap<>();
            sysPartMap.put("text", systemPrompt);
            sysParts.add(sysPartMap);
            systemInstruction.put("parts", sysParts);
            requestBody.put("systemInstruction", systemInstruction);

            // Force JSON output
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);

            log.info("Sending decision request to Gemini API...");
            String responseJson = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/gemini-1.5-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            String textResult = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText();

            log.debug("Received decision text from LLM: {}", textResult);

            // Parse response back to BotDecision record
            JsonNode decisionNode = objectMapper.readTree(textResult);
            JsonNode actionNode = decisionNode.path("action");
            
            String actionTypeStr = actionNode.path("type").asText("WAIT");
            mk.ukim.finki.aibotbackend.model.enums.BotActionType actionType = 
                    mk.ukim.finki.aibotbackend.model.enums.BotActionType.valueOf(actionTypeStr.toUpperCase());
            
            String target = actionNode.has("target") && !actionNode.path("target").isNull() ? actionNode.path("target").asText() : null;
            String value = actionNode.has("value") && !actionNode.path("value").isNull() ? actionNode.path("value").asText() : null;
            String reasoning = actionNode.has("reasoning") && !actionNode.path("reasoning").isNull() ? actionNode.path("reasoning").asText() : null;
            
            BotAction botAction = new BotAction(actionType, target, value, reasoning);
            
            boolean goalReached = decisionNode.path("goalReached").asBoolean(false);
            String rationale = decisionNode.path("rationale").asText("");

            return new BotDecision(botAction, goalReached, rationale);

        } catch (Exception e) {
            log.error("Failed to get/parse LLM decision", e);
            // Default to waiting or finishing on error rather than breaking the application loop
            return new BotDecision(
                new BotAction(mk.ukim.finki.aibotbackend.model.enums.BotActionType.WAIT, null, null, "Error calling LLM, fallback to wait"),
                false,
                "LLM communication error: " + e.getMessage()
            );
        }
    }
}
