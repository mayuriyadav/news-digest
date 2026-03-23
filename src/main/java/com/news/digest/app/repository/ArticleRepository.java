package com.news.digest.app.repository;

import com.news.digest.app.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository  extends JpaRepository<Article, Long> {

    // Basic queries
    Optional<Article> findByUrl(String url);

    boolean existsByUrl(String url);

    // Find by category
    Page<Article> findByCategory(String category, Pageable pageable);

    Page<Article> findByCategoryIn(List<String> categories, Pageable pageable);

    // Find by source
    Page<Article> findBySourceId(Long sourceId, Pageable pageable);

    Page<Article> findBySourceName(String sourceName, Pageable pageable);

    // Find by author
    Page<Article> findByAuthor(String author, Pageable pageable);

    // Find by language
    Page<Article> findByLanguage(String language, Pageable pageable);

    // Find by country
    Page<Article> findByCountry(String country, Pageable pageable);

    // Search queries
    @Query("SELECT a FROM Article a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> searchArticles(@Param("keyword") String keyword, Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT a FROM Article a WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR a.category = :category) AND " +
            "(:sourceId IS NULL OR a.source.id = :sourceId) AND " +
            "(:author IS NULL OR a.author = :author) AND " +
            "(:language IS NULL OR a.language = :language) AND " +
            "(:country IS NULL OR a.country = :country) AND " +
            "(:startDate IS NULL OR a.publishedAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.publishedAt <= :endDate)")
    Page<Article> advancedSearch(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("sourceId") Long sourceId,
            @Param("author") String author,
            @Param("language") String language,
            @Param("country") String country,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Get all distinct categories
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.category IS NOT NULL")
    List<String> findAllCategories();

    // Get all distinct languages
    @Query("SELECT DISTINCT a.language FROM Article a WHERE a.language IS NOT NULL")
    List<String> findAllLanguages();

    // Get all distinct countries
    @Query("SELECT DISTINCT a.country FROM Article a WHERE a.country IS NOT NULL")
    List<String> findAllCountries();

    // Get all distinct sources
    @Query("SELECT DISTINCT a.sourceName FROM Article a WHERE a.sourceName IS NOT NULL")
    List<String> findAllSourceNames();

    // Trending articles (based on views, likes, bookmarks in last 7 days)
    @Query("SELECT a FROM Article a WHERE a.publishedAt >= :since " +
            "ORDER BY (a.viewCount + a.likeCount * 2 + a.bookmarkCount * 3) DESC")
    Page<Article> findTrendingArticles(@Param("since") LocalDateTime since, Pageable pageable);

    // Most viewed articles
    Page<Article> findAllByOrderByViewCountDesc(Pageable pageable);

    // Most liked articles
    Page<Article> findAllByOrderByLikeCountDesc(Pageable pageable);

    // Most bookmarked articles
    Page<Article> findAllByOrderByBookmarkCountDesc(Pageable pageable);

    // Most shared articles
    Page<Article> findAllByOrderByShareCountDesc(Pageable pageable);

    // Latest articles
    Page<Article> findAllByOrderByPublishedAtDesc(Pageable pageable);

    // Featured articles
    Page<Article> findByIsFeaturedTrue(Pageable pageable);

    // Breaking news
    Page<Article> findByIsBreakingTrue(Pageable pageable);

    // Premium articles
    Page<Article> findByIsPremiumTrue(Pageable pageable);

    // Articles published between dates
    Page<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Articles with sentiment score above threshold
    Page<Article> findBySentimentScoreGreaterThan(Double threshold, Pageable pageable);

    // Count articles by category
    @Query("SELECT a.category, COUNT(a) FROM Article a GROUP BY a.category")
    List<Object[]> countArticlesByCategory();

    // Count articles by source
    @Query("SELECT a.sourceName, COUNT(a) FROM Article a GROUP BY a.sourceName")
    List<Object[]> countArticlesBySource();

    // Count articles by language
    @Query("SELECT a.language, COUNT(a) FROM Article a GROUP BY a.language")
    List<Object[]> countArticlesByLanguage();

    // Update view count
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Update like count
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // Update bookmark count
    @Query("UPDATE Article a SET a.bookmarkCount = a.bookmarkCount + 1 WHERE a.id = :id")
    void incrementBookmarkCount(@Param("id") Long id);

    // Update share count
    @Query("UPDATE Article a SET a.shareCount = a.shareCount + 1 WHERE a.id = :id")
    void incrementShareCount(@Param("id") Long id);
}
