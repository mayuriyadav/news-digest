package com.news.digest.app.Agent;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.digest.app.config.NewsApiClient;
import com.news.digest.app.model.NewsApiArticle;
import com.news.digest.app.service.impl.NewsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsSearchAgent {

    private final NewsApiClient newsApiClient;
    private final NewsIngestionService newsIngestionService;
    private final ObjectMapper objectMapper;

    @Value("${agent.api.base-url:https://generativelanguage.googleapis.com/v1beta/openai}")
    private String apiBaseUrl;

    @Value("${agent.api.key:${AGENT_API_KEY:}}")
    private String apiKey;

    @Value("${agent.api.model:gemini-1.5-flash}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            You are a news-search planning assistant.
            Given a user query, produce a JSON object with ONE of these two fields (never both):

            Option A – keyword search:
            { "type": "keyword", "keyword": "<search term>", "language": "en", "pageSize": 20 }

            Option B – category headlines:
            { "type": "headlines", "category": "<one of: technology|business|health|science|sports|entertainment|general>", "country": "us", "pageSize": 20 }

            Rules:
            - Use "headlines" for broad category requests ("latest sports news", "top business stories").
            - Use "keyword" for specific topics ("Bitcoin price", "India election results", "ChatGPT update").
            - Reply with ONLY the JSON object. No explanation, no markdown fences.
            """;

    public AgentResult search(String userQuery) {
        log.info("Agent received query: {}", userQuery);

        String planJson = askLlmForPlan(userQuery);
        log.info("LLM plan result: {}", planJson);

        // Always fallback — never return failure just because LLM failed
        if (planJson == null) {
            log.warn("LLM returned null — falling back to keyword search");
            planJson = buildFallback(userQuery);
        }

        try {
            JsonNode plan = objectMapper.readTree(planJson);
            String type = plan.path("type").asText();

            if ("keyword".equals(type)) {
                String keyword  = plan.path("keyword").asText();
                String language = plan.path("language").asText("en");
                int    pageSize = plan.path("pageSize").asInt(20);

                List<NewsApiArticle> fetched = newsApiClient.searchEverything(keyword, language, pageSize);
                int saved = newsIngestionService.ingestByKeyword(keyword, language, pageSize);

                return AgentResult.success("keyword", keyword, fetched.size(), saved,
                        "Searched NewsAPI for: \"" + keyword + "\"");

            } else if ("headlines".equals(type)) {
                String category = plan.path("category").asText("general");
                String country  = plan.path("country").asText("us");
                int    pageSize = plan.path("pageSize").asInt(20);

                List<NewsApiArticle> fetched = newsApiClient.fetchTopHeadlines(category, country, pageSize);
                int saved = newsIngestionService.ingestTopHeadlines(category, country, pageSize);

                return AgentResult.success("headlines", category, fetched.size(), saved,
                        "Fetched top headlines for category: \"" + category + "\"");

            } else {
                return AgentResult.failure("Unknown plan type from LLM: " + type);
            }

        } catch (Exception e) {
            log.error("Agent execution failed: {}", e.getMessage(), e);
            return AgentResult.failure("Execution error: " + e.getMessage());
        }
    }

    private String askLlmForPlan(String userQuery) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("agent.api.key is blank — using fallback keyword plan");
            return buildFallback(userQuery);
        }

        log.info("Calling LLM: baseUrl={}, model={}", apiBaseUrl, model);

        int maxRetries = 3;
        int delayMs    = 10000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                WebClient client = WebClient.builder()
                        .baseUrl(apiBaseUrl)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();

                Map<String, Object> body = Map.of(
                        "model", model,
                        "max_tokens", 200,
                        "messages", List.of(
                                Map.of("role", "system", "content", SYSTEM_PROMPT),
                                Map.of("role", "user",   "content", userQuery)
                        )
                );

                String response = client.post()
                        .uri("/chat/completions")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Raw LLM response: {}", response);

                if (response == null) {
                    log.error("LLM returned null response body");
                    return null;
                }

                JsonNode root = objectMapper.readTree(response);

                if (root.has("error")) {
                    log.error("LLM API error in body: {}", root.path("error").toString());
                    return null;
                }

                JsonNode choices = root.path("choices");
                if (choices.isMissingNode() || choices.isEmpty()) {
                    log.error("No 'choices' in LLM response: {}", response);
                    return null;
                }

                String content = choices.get(0).path("message").path("content").asText().trim();
                log.info("LLM content: {}", content);

                // Strip markdown fences if Gemini wrapped JSON anyway
                if (content.startsWith("```")) {
                    content = content.replaceAll("```json", "").replaceAll("```", "").trim();
                }

                return content;

            } catch (WebClientResponseException e) {
                log.error("HTTP {} from LLM: {}", e.getStatusCode().value(), e.getResponseBodyAsString());

                if (e.getStatusCode().value() == 429 && attempt < maxRetries) {
                    log.warn("Rate limit — retrying in {}ms (attempt {}/{})", delayMs, attempt, maxRetries);
                    try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    delayMs *= 2; // 10s → 20s → 40s
                } else if (e.getStatusCode().value() == 429) {
                    log.warn("Rate limit after {} retries — falling back to keyword search", maxRetries);
                    return buildFallback(userQuery);
                } else if (e.getStatusCode().value() == 401) {
                    log.error("Invalid API key — check agent.api.key in application.properties");
                    return null;
                } else {
                    return null;
                }

            } catch (Exception e) {
                log.error("LLM call failed (attempt {}): {}", attempt, e.getMessage());
                return null;
            }
        }
        return null;
    }

    private String buildFallback(String userQuery) {
        String safe = userQuery.replace("\"", "'");
        return "{\"type\":\"keyword\",\"keyword\":\"" + safe + "\",\"language\":\"en\",\"pageSize\":20}";
    }

    public record AgentResult(
            boolean success,
            String  message,
            String  searchType,
            String  searchValue,
            int     fetchedCount,
            int     savedCount
    ) {
        static AgentResult success(String type, String value, int fetched, int saved, String msg) {
            return new AgentResult(true, msg, type, value, fetched, saved);
        }
        static AgentResult failure(String msg) {
            return new AgentResult(false, msg, null, null, 0, 0);
        }
    }
}