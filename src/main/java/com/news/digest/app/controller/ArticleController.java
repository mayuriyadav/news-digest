package com.news.digest.app.controller;




import com.news.digest.app.dto.*;
import com.news.digest.app.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

private final ArticleService articleService;

    /** Reads userId embedded in JWT by JwtAuthenticationFilter */
    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(
            @Valid @RequestBody ArticleRequestDTO articleRequest) {
        ArticleDTO created = articleService.createArticle(articleRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(
            @PathVariable Long id, HttpServletRequest request) {
        ArticleDTO article = articleService.getArticleById(id, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Article fetched successfully", article));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequestDTO articleRequest) {
        ArticleDTO updated = articleService.updateArticle(id, articleRequest);
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }

    // ==================== BASIC QUERIES ====================

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getAllArticles(page, size, sortBy, sortDir, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles fetched successfully", articles));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesByCategory(category, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles fetched by category", articles));
    }

    @GetMapping("/source/{sourceId}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesBySource(
            @PathVariable Long sourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesBySource(sourceId, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles fetched by source", articles));
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesByAuthor(author, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles fetched by author", articles));
    }

    // ==================== SEARCH ====================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.searchArticles(keyword, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Search results", articles));
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> advancedSearch(
            @RequestBody ArticleSearchDTO searchDTO,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.advancedSearch(searchDTO, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Advanced search results", articles));
    }

    // ==================== SPECIAL COLLECTIONS ====================

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getTrendingArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getTrendingArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Trending articles", articles));
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostViewedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getMostViewedArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Most viewed articles", articles));
    }

    @GetMapping("/most-liked")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostLikedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getMostLikedArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Most liked articles", articles));
    }

    @GetMapping("/most-bookmarked")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostBookmarkedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getMostBookmarkedArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Most bookmarked articles", articles));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getLatestArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getLatestArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Latest articles", articles));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getFeaturedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getFeaturedArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Featured articles", articles));
    }

    @GetMapping("/breaking")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getBreakingNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getBreakingNews(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Breaking news", articles));
    }

    @GetMapping("/premium")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getPremiumArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getPremiumArticles(page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Premium articles", articles));
    }

    // ==================== LANGUAGE / COUNTRY / DATE ====================

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesByLanguage(language, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles by language", articles));
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByCountry(
            @PathVariable String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesByCountry(country, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles by country", articles));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<ArticleDTO> articles = articleService.getArticlesByDateRange(start, end, page, size, getUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Articles by date range", articles));
    }

    // ==================== METADATA ====================

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", articleService.getAllCategories()));
    }

    @GetMapping("/sources")
    public ResponseEntity<ApiResponse<List<String>>> getAllSources() {
        return ResponseEntity.ok(ApiResponse.success("Sources fetched", articleService.getAllSources()));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<String>>> getAllLanguages() {
        return ResponseEntity.ok(ApiResponse.success("Languages fetched", articleService.getAllLanguages()));
    }

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<String>>> getAllCountries() {
        return ResponseEntity.ok(ApiResponse.success("Countries fetched", articleService.getAllCountries()));
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArticleStatistics() {
        return ResponseEntity.ok(ApiResponse.success("Statistics fetched", articleService.getArticleStatistics()));
    }

    @GetMapping("/statistics/category-wise")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCategoryWiseCount() {
        return ResponseEntity.ok(ApiResponse.success("Category-wise count", articleService.getCategoryWiseCount()));
    }

    // ==================== ENGAGEMENT (require auth) ====================

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeArticle(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        articleService.likeArticle(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Article liked", null));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeArticle(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        articleService.unlikeArticle(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Like removed", null));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Void>> bookmarkArticle(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        articleService.bookmarkArticle(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Article bookmarked", null));
    }

    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        articleService.removeBookmark(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed", null));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<Void>> shareArticle(
            @PathVariable Long id,
            @RequestParam String platform,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        articleService.shareArticle(id, userId, platform);
        return ResponseEntity.ok(ApiResponse.success("Article shared", null));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> recordView(
            @PathVariable Long id,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer scrollDepth,
            HttpServletRequest request) {
        articleService.recordView(id, getUserId(request), duration, scrollDepth);
        return ResponseEntity.ok(ApiResponse.success("View recorded", null));
    }

    // ==================== STATUS CHECKS ====================

    @GetMapping("/{id}/liked")
    public ResponseEntity<ApiResponse<Boolean>> isArticleLiked(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.success("Not authenticated", false));
        return ResponseEntity.ok(ApiResponse.success("Like status", articleService.isArticleLikedByUser(id, userId)));
    }

    @GetMapping("/{id}/bookmarked")
    public ResponseEntity<ApiResponse<Boolean>> isArticleBookmarked(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.success("Not authenticated", false));
        return ResponseEntity.ok(ApiResponse.success("Bookmark status", articleService.isArticleBookmarkedByUser(id, userId)));
    }


    // ── Reading History ──────────────────────────────────────────────────────

    @GetMapping("/reading-history")
    public ResponseEntity<ApiResponse<Page<ReadingHistoryDTO>>> getReadingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("Reading history fetched",
                articleService.getReadingHistory(userId, page, size)));
    }

    @GetMapping("/reading-activity")
    public ResponseEntity<ApiResponse<ReadingActivityDTO>> getReadingActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("Reading activity fetched",
                articleService.getReadingActivity(userId, start, end)));
    }

    // ── Bookmarks ────────────────────────────────────────────────────────────

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<Page<BookmarkDTO>>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("Bookmarks fetched",
                articleService.getBookmarks(userId, page, size)));
    }

    @GetMapping("/bookmarks/folder/{folder}")
    public ResponseEntity<ApiResponse<Page<BookmarkDTO>>> getBookmarksByFolder(
            @PathVariable String folder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("Bookmarks fetched",
                articleService.getBookmarksByFolder(userId, folder, page, size)));
    }

    @GetMapping("/bookmarks/folders")
    public ResponseEntity<ApiResponse<List<String>>> getBookmarkFolders(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("Folders fetched",
                articleService.getBookmarkFolders(userId)));
    }

    // ── User Stats ───────────────────────────────────────────────────────────

    @GetMapping("/user-stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        return ResponseEntity.ok(ApiResponse.success("User stats fetched",
                articleService.getUserStats(userId)));
    }

}
