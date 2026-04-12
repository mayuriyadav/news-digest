package com.news.digest.app.config;

import com.news.digest.app.dto.NewsApiResponse;
import com.news.digest.app.model.NewsApiArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j

public class NewsApiClient {

    private final WebClient newsApiWebClient;

    @Value("${newsapi.default-page-size:20}")
    private int defaultPageSize;

    /**
     * Fetch top headlines by category and country.
     * GET /v2/top-headlines?category=technology&country=us&pageSize=20
     */
    public List<NewsApiArticle> fetchTopHeadlines(String category, String country, int pageSize) {
        try {
            log.debug("Fetching top headlines: category={}, country={}", category, country);

            NewsApiResponse response = newsApiWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/top-headlines")
                            .queryParam("category", category)
                            .queryParam("country", country)
                            .queryParam("pageSize", pageSize)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiResponse.class)
                    .block(); // sync — scheduler runs on its own thread

            if (response == null || response.getArticles() == null) {
                log.warn("Empty response from NewsAPI for category: {}", category);
                return Collections.emptyList();
            }

            log.info("Fetched {} articles for category: {}", response.getArticles().size(), category);
            return response.getArticles();

        } catch (WebClientResponseException e) {
            log.error("NewsAPI HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("NewsAPI fetch failed for category {}: {}", category, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Search everything by keyword.
     * GET /v2/everything?q=bitcoin&language=en&pageSize=20
     */
    public List<NewsApiArticle> searchEverything(String keyword, String language, int pageSize) {
        try {
            log.debug("Searching NewsAPI for: {}", keyword);

            NewsApiResponse response = newsApiWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", keyword)
                            .queryParam("language", language)
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", pageSize)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiResponse.class)
                    .block();

            if (response == null || response.getArticles() == null) return Collections.emptyList();

            return response.getArticles();

        } catch (Exception e) {
            log.error("NewsAPI search failed for keyword {}: {}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }
}
