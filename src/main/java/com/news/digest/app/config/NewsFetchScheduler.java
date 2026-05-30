package com.news.digest.app.config;


import com.news.digest.app.serviceimpl.NewsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "newsapi.fetch.enabled", havingValue = "true")

public class NewsFetchScheduler {

    private final NewsIngestionService newsIngestionService;

    @Value("${newsapi.fetch.categories:technology,business,health,science,sports,entertainment}")
    private String categoriesConfig;

    @Value("${newsapi.fetch.country:us}")
    private String country;

    @Value("${newsapi.fetch.language:en}")
    private String language;

    @Value("${newsapi.default-page-size:20}")
    private int pageSize;

    /**
     * Runs on schedule defined in application.properties (default: every 2 hours).
     * Fetches top headlines for all configured categories.
     */
    @Scheduled(cron = "${newsapi.fetch.cron:0 0 */2 * * *}")
    public void fetchAllCategories() {
        List<String> categories = Arrays.asList(categoriesConfig.split(","));
        log.info("Scheduled fetch starting — {} categories", categories.size());

        int totalSaved = 0;
        for (String category : categories) {
            try {
                int saved = newsIngestionService.ingestTopHeadlines(
                        category.trim(), country, pageSize);
                totalSaved += saved;

                // Small pause between categories — avoids hitting rate limit
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Failed to fetch category {}: {}", category, e.getMessage());
            }
        }

        log.info("Scheduled fetch complete — {} new articles saved", totalSaved);
    }
}
