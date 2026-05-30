package com.news.digest.app.controller;

import com.news.digest.app.config.NewsFetchScheduler;
import com.news.digest.app.dto.ApiResponse;
import com.news.digest.app.serviceimpl.NewsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/news-fetch")
@RequiredArgsConstructor
public class NewsApiController {

    private final NewsIngestionService newsIngestionService;
    private final NewsFetchScheduler newsFetchScheduler;

    /**
     * Manually trigger a full fetch for all categories.
     * POST /api/news-fetch/trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerFetch() {
        newsFetchScheduler.fetchAllCategories();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Fetch triggered successfully");
        return ResponseEntity.ok(ApiResponse.success("News fetch triggered", result));
    }

    /**
     * Fetch a specific category on demand.
     * POST /api/news-fetch/category?name=technology&country=us&pageSize=20
     */
    @PostMapping("/category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchCategory(
            @RequestParam String name,
            @RequestParam(defaultValue = "us") String country,
            @RequestParam(defaultValue = "20") int pageSize) {

        int saved = newsIngestionService.ingestTopHeadlines(name, country, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("category", name);
        result.put("newArticlesSaved", saved);
        return ResponseEntity.ok(ApiResponse.success("Category fetched", result));
    }

    /**
     * Search and import live results from NewsAPI by keyword.
     * POST /api/news-fetch/search?keyword=bitcoin&language=en&pageSize=20
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "20") int pageSize) {

        int saved = newsIngestionService.ingestByKeyword(keyword, language, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", keyword);
        result.put("newArticlesSaved", saved);
        return ResponseEntity.ok(ApiResponse.success("Keyword search ingested", result));
    }
}
