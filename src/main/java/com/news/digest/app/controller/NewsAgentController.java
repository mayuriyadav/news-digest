package com.news.digest.app.controller;


import com.news.digest.app.Agent.NewsSearchAgent;
import com.news.digest.app.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * Exposes the AI News Search Agent via REST.
 *
 * POST /api/agent/search?query=latest+AI+news
 *
 * The agent:
 *   1. Sends your natural-language query to an LLM
 *   2. LLM decides whether to do a keyword search or category headlines fetch
 *   3. Agent calls NewsAPI, saves new articles, returns a summary
 */

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class NewsAgentController {


    private final NewsSearchAgent newsSearchAgent;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<NewsSearchAgent.AgentResult>> agentSearch(
            @RequestParam String query) {

        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Query must not be empty"));
        }

        NewsSearchAgent.AgentResult result = newsSearchAgent.search(query);

        if (result.success()) {
            return ResponseEntity.ok(ApiResponse.success("Agent search complete", result));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(result.message()));
        }
    }

    // ── Batch search — multiple queries at once ───────────────────────────────

    /**
     * POST /api/agent/search/batch
     * Body: { "queries": ["AI news", "cricket India", "Bitcoin price"] }
     * Runs each query through the agent and returns all results
     */
    @PostMapping("/search/batch")
    public ResponseEntity<ApiResponse<List<NewsSearchAgent.AgentResult>>> batchSearch(
            @RequestBody Map<String, List<String>> body) {

        List<String> queries = body.get("queries");

        if (queries == null || queries.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("'queries' list must not be empty"));
        }

        if (queries.size() > 5) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Maximum 5 queries per batch to avoid rate limiting"));
        }

        List<NewsSearchAgent.AgentResult> results = queries.stream()
                .map(q -> {
                    try {
                        // Small delay between batch queries to respect rate limits
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return newsSearchAgent.search(q);
                })
                .toList();

        long successCount = results.stream().filter(NewsSearchAgent.AgentResult::success).count();

        return ResponseEntity.ok(ApiResponse.success(
                successCount + "/" + queries.size() + " searches completed", results));
    }

    // ── Direct category headlines fetch ──────────────────────────────────────

    /**
     * GET /api/agent/search/category/technology
     * Skips Gemini — directly fetches top headlines for a known category
     * Categories: technology | business | health | science | sports | entertainment | general
     */
    @GetMapping("/search/category/{category}")
    public ResponseEntity<ApiResponse<NewsSearchAgent.AgentResult>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "us") String country) {

        List<String> validCategories = List.of(
                "technology", "business", "health", "science",
                "sports", "entertainment", "general");

        if (!validCategories.contains(category.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid category. Valid: " + validCategories));
        }

        // Build query so Gemini picks "headlines" — or just use direct keyword
        NewsSearchAgent.AgentResult result = newsSearchAgent.search("top " + category + " news");

        if (result.success()) {
            return ResponseEntity.ok(ApiResponse.success("Category headlines fetched", result));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(result.message()));
        }
    }

    // ── Agent capabilities info ───────────────────────────────────────────────

    /**
     * GET /api/agent/capabilities
     * Returns what the agent can do — useful for frontend to know available features
     */
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCapabilities() {
        Map<String, Object> capabilities = Map.of(
                "description", "AI-powered news search agent using Gemini LLM",
                "endpoints", List.of(
                        Map.of("method", "POST", "path", "/api/agent/search?query=",
                                "description", "Natural language search — Gemini picks the best strategy"),
                        Map.of("method", "POST", "path", "/api/agent/search/batch",
                                "description", "Search multiple queries at once (max 5)"),
                        Map.of("method", "GET",  "path", "/api/agent/search/category/{category}",
                                "description", "Fetch top headlines for a specific category")
                ),
                "supportedCategories", List.of(
                        "technology", "business", "health", "science",
                        "sports", "entertainment", "general"),
                "exampleQueries", List.of(
                        "latest cricket news in India",
                        "Bitcoin price today",
                        "ChatGPT new features",
                        "top business stories",
                        "health news today")
        );

        return ResponseEntity.ok(ApiResponse.success("Agent capabilities", capabilities));
    }
}
