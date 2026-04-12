package com.news.digest.app.service.impl;


import com.news.digest.app.config.NewsApiClient;
import com.news.digest.app.model.Article;
import com.news.digest.app.model.NewsApiArticle;
import com.news.digest.app.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsIngestionService {

    private final ArticleRepository articleRepository;
    private final NewsApiClient newsApiClient;

    /**
     * Fetch and store top headlines for a given category.
     * Returns count of newly saved articles.
     */
    @Transactional
    public int ingestTopHeadlines(String category, String country, int pageSize) {
        List<NewsApiArticle> articles = newsApiClient.fetchTopHeadlines(category, country, pageSize);
        return saveArticles(articles, category);
    }

    /**
     * Fetch and store articles by keyword search.
     */
    @Transactional
    public int ingestByKeyword(String keyword, String language, int pageSize) {
        List<NewsApiArticle> articles = newsApiClient.searchEverything(keyword, language, pageSize);
        return saveArticles(articles, null);
    }

    private int saveArticles(List<NewsApiArticle> articles, String category) {
        AtomicInteger savedCount = new AtomicInteger(0);

        for (NewsApiArticle apiArticle : articles) {
            try {
                // Skip articles with missing required fields
                if (apiArticle.getUrl() == null || apiArticle.getTitle() == null) continue;
                if (apiArticle.getTitle().equals("[Removed]")) continue;

                // Skip duplicates — URL is unique key
                if (articleRepository.existsByUrl(apiArticle.getUrl())) continue;

                Article article = mapToArticle(apiArticle, category);
                articleRepository.save(article);
                savedCount.incrementAndGet();

            } catch (Exception e) {
                log.warn("Failed to save article '{}': {}", apiArticle.getTitle(), e.getMessage());
            }
        }

        log.info("Ingested {}/{} new articles for category: {}", savedCount.get(), articles.size(), category);
        return savedCount.get();
    }

    private Article mapToArticle(NewsApiArticle apiArticle, String category) {
        Article article = new Article();

        article.setTitle(trimTo(apiArticle.getTitle(), 500));
        article.setDescription(apiArticle.getDescription());

        // NewsAPI truncates content with "[+N chars]" — strip it
        String content = apiArticle.getContent();
        if (content != null && content.contains("[+")) {
            content = content.substring(0, content.lastIndexOf("[+")).trim();
        }
        article.setContent(content);

        article.setUrl(trimTo(apiArticle.getUrl(), 500));
        article.setImageUrl(trimTo(apiArticle.getUrlToImage(), 500));
        article.setAuthor(trimTo(apiArticle.getAuthor(), 200));

        // Source info
        if (apiArticle.getSource() != null) {
            article.setSourceName(apiArticle.getSource().getName());
        }

        // Published time
        if (apiArticle.getPublishedAt() != null) {
            article.setPublishedAt(apiArticle.getPublishedAt().toLocalDateTime());
        }

        article.setCategory(category);
        article.setLanguage("en");
        article.setFetchedAt(LocalDateTime.now());

        // Defaults — @PrePersist will also handle these but being explicit is safe
        article.setIsActive(true);
        article.setIsFeatured(false);
        article.setIsBreaking(false);
        article.setIsPremium(false);

        return article;
    }

    private String trimTo(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
