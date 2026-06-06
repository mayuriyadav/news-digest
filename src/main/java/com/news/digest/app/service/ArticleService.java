package com.news.digest.app.service;

import com.news.digest.app.dto.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ArticleService {

    ArticleDTO createArticle(ArticleRequestDTO articleRequest);
    ArticleDTO getArticleById(Long id, Long userId);
    ArticleDTO updateArticle(Long id, ArticleRequestDTO articleRequest);
    void deleteArticle(Long id);

    // Get all articles with pagination
    Page<ArticleDTO> getAllArticles(int page, int size, String sortBy, String sortDir, Long userId);

    // Get articles by category
    Page<ArticleDTO> getArticlesByCategory(String category, int page, int size, Long userId);

    // Get articles by source
    Page<ArticleDTO> getArticlesBySource(Long sourceId, int page, int size, Long userId);

    // Get articles by author
    Page<ArticleDTO> getArticlesByAuthor(String author, int page, int size, Long userId);

    // Search articles
    Page<ArticleDTO> searchArticles(String keyword, int page, int size, Long userId);

    // Advanced search
    Page<ArticleDTO> advancedSearch(ArticleSearchDTO searchDTO, Long userId);

    // Get trending articles
    Page<ArticleDTO> getTrendingArticles(int page, int size, Long userId);

    // Get most viewed articles
    Page<ArticleDTO> getMostViewedArticles(int page, int size, Long userId);

    // Get most liked articles
    Page<ArticleDTO> getMostLikedArticles(int page, int size, Long userId);

    // Get most bookmarked articles
    Page<ArticleDTO> getMostBookmarkedArticles(int page, int size, Long userId);

    // Get latest articles
    Page<ArticleDTO> getLatestArticles(int page, int size, Long userId);

    // Get featured articles
    Page<ArticleDTO> getFeaturedArticles(int page, int size, Long userId);

    // Get breaking news
    Page<ArticleDTO> getBreakingNews(int page, int size, Long userId);

    // Get premium articles
    Page<ArticleDTO> getPremiumArticles(int page, int size, Long userId);

    // Get articles by language
    Page<ArticleDTO> getArticlesByLanguage(String language, int page, int size, Long userId);

    // Get articles by country
    Page<ArticleDTO> getArticlesByCountry(String country, int page, int size, Long userId);

    // Get articles by date range
    Page<ArticleDTO> getArticlesByDateRange(LocalDateTime start, LocalDateTime end, int page, int size, Long userId);

    // Get all categories
    List<String> getAllCategories();

    // Get all sources
    List<String> getAllSources();

    // Get all languages
    List<String> getAllLanguages();

    // Get all countries
    List<String> getAllCountries();

    // Get article statistics
    Map<String, Object> getArticleStatistics();

    // Get category-wise article count
    Map<String, Long> getCategoryWiseCount();

    // Get source-wise article count
    Map<String, Long> getSourceWiseCount();

    // Get language-wise article count
    Map<String, Long> getLanguageWiseCount();

    // Engagement methods
    void likeArticle(Long articleId, Long userId);
    void unlikeArticle(Long articleId, Long userId);
    void bookmarkArticle(Long articleId, Long userId);
    void removeBookmark(Long articleId, Long userId);
    void shareArticle(Long articleId, Long userId, String platform);
    void recordView(Long articleId, Long userId, Integer readDuration, Integer scrollDepth);

    // Check engagement status
    boolean isArticleLikedByUser(Long articleId, Long userId);
    boolean isArticleBookmarkedByUser(Long articleId, Long userId);
    boolean isArticleReadByUser(Long articleId, Long userId);
    Page<ReadingHistoryDTO> getReadingHistory(Long userId, int page, int size);
    ReadingActivityDTO getReadingActivity(Long userId, LocalDateTime start, LocalDateTime end);

    // ── Bookmarks ────────────────────────────────────────────────────────────
    Page<BookmarkDTO> getBookmarks(Long userId, int page, int size);
    Page<BookmarkDTO> getBookmarksByFolder(Long userId, String folder, int page, int size);
    List<String> getBookmarkFolders(Long userId);

    // ── User Stats ───────────────────────────────────────────────────────────
    UserStatsDTO getUserStats(Long userId);

}
