package com.news.digest.app.controller;




import com.news.digest.app.dto.ApiResponse;
import com.news.digest.app.dto.ArticleDTO;
import com.news.digest.app.dto.ArticleRequestDTO;
import com.news.digest.app.dto.ArticleSearchDTO;
import com.news.digest.app.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    // ==================== CRUD ====================

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
}

//
//    // Helper method to extract user ID from UserDetails
//    private Long getUserId(UserDetails userDetails) {
//        if (userDetails == null) return null;
//        // You need to implement this based on how you store user ID
//        // For now, return null or implement properly
//        return null;
//    }
//
//
//
//    @PostMapping
//    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(
//            @Valid @RequestBody ArticleRequestDTO articleRequest) {
//        ArticleDTO created = articleService.createArticle(articleRequest);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success("Article created successfully", created));
//    }
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        ArticleDTO article = articleService.getArticleById(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Article fetched successfully", article));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
//            @PathVariable Long id,
//            @Valid @RequestBody ArticleRequestDTO articleRequest) {
//        ArticleDTO updated = articleService.updateArticle(id, articleRequest);
//        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", updated));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
//        articleService.deleteArticle(id);
//        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
//    }
//
//    // ==================== BASIC QUERIES ====================
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getAllArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "publishedAt") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getAllArticles(page, size, sortBy, sortDir, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles fetched successfully", articles));
//    }
//
//    @GetMapping("/category/{category}")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByCategory(
//            @PathVariable String category,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesByCategory(category, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles fetched by category", articles));
//    }
//
//    @GetMapping("/source/{sourceId}")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesBySource(
//            @PathVariable Long sourceId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesBySource(sourceId, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles fetched by source", articles));
//    }
//
//    @GetMapping("/author/{author}")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByAuthor(
//            @PathVariable String author,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesByAuthor(author, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles fetched by author", articles));
//    }
//
//    // ==================== SEARCH ====================
//
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> searchArticles(
//            @RequestParam String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.searchArticles(keyword, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Search results", articles));
//    }
//
//    @PostMapping("/advanced-search")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> advancedSearch(
//            @RequestBody ArticleSearchDTO searchDTO,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.advancedSearch(searchDTO, userId);
//        return ResponseEntity.ok(ApiResponse.success("Advanced search results", articles));
//    }
//
//    // ==================== SPECIAL COLLECTIONS ====================
//
//    @GetMapping("/trending")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getTrendingArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getTrendingArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Trending articles", articles));
//    }
//
//    @GetMapping("/most-viewed")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostViewedArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getMostViewedArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Most viewed articles", articles));
//    }
//
//    @GetMapping("/most-liked")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostLikedArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getMostLikedArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Most liked articles", articles));
//    }
//
//    @GetMapping("/most-bookmarked")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getMostBookmarkedArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getMostBookmarkedArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Most bookmarked articles", articles));
//    }
//
//    @GetMapping("/latest")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getLatestArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getLatestArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Latest articles", articles));
//    }
//
//    @GetMapping("/featured")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getFeaturedArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getFeaturedArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Featured articles", articles));
//    }
//
//    @GetMapping("/breaking")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getBreakingNews(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getBreakingNews(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Breaking news", articles));
//    }
//
//    @GetMapping("/premium")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getPremiumArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getPremiumArticles(page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Premium articles", articles));
//    }
//
//    // ==================== LANGUAGE/COUNTRY ====================
//
//    @GetMapping("/language/{language}")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByLanguage(
//            @PathVariable String language,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesByLanguage(language, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles by language", articles));
//    }
//
//    @GetMapping("/country/{country}")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByCountry(
//            @PathVariable String country,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesByCountry(country, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles by country", articles));
//    }
//
//    @GetMapping("/date-range")
//    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByDateRange(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        Page<ArticleDTO> articles = articleService.getArticlesByDateRange(start, end, page, size, userId);
//        return ResponseEntity.ok(ApiResponse.success("Articles by date range", articles));
//    }
//
//    // ==================== METADATA ====================
//
//    @GetMapping("/categories")
//    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
//        List<String> categories = articleService.getAllCategories();
//        return ResponseEntity.ok(ApiResponse.success("Categories fetched", categories));
//    }
//
//    @GetMapping("/sources")
//    public ResponseEntity<ApiResponse<List<String>>> getAllSources() {
//        List<String> sources = articleService.getAllSources();
//        return ResponseEntity.ok(ApiResponse.success("Sources fetched", sources));
//    }
//
//    @GetMapping("/languages")
//    public ResponseEntity<ApiResponse<List<String>>> getAllLanguages() {
//        List<String> languages = articleService.getAllLanguages();
//        return ResponseEntity.ok(ApiResponse.success("Languages fetched", languages));
//    }
//
//    @GetMapping("/countries")
//    public ResponseEntity<ApiResponse<List<String>>> getAllCountries() {
//        List<String> countries = articleService.getAllCountries();
//        return ResponseEntity.ok(ApiResponse.success("Countries fetched", countries));
//    }
//
//    // ==================== STATISTICS ====================
//
//    @GetMapping("/statistics")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> getArticleStatistics() {
//        Map<String, Object> stats = articleService.getArticleStatistics();
//        return ResponseEntity.ok(ApiResponse.success("Statistics fetched", stats));
//    }
//
//    @GetMapping("/statistics/category-wise")
//    public ResponseEntity<ApiResponse<Map<String, Long>>> getCategoryWiseCount() {
//        Map<String, Long> stats = articleService.getCategoryWiseCount();
//        return ResponseEntity.ok(ApiResponse.success("Category-wise count", stats));
//    }
//
//    // ==================== ENGAGEMENT ====================
//
//    @PostMapping("/{id}/like")
//    public ResponseEntity<ApiResponse<Void>> likeArticle(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        articleService.likeArticle(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Article liked", null));
//    }
//
//    @DeleteMapping("/{id}/like")
//    public ResponseEntity<ApiResponse<Void>> unlikeArticle(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        articleService.unlikeArticle(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Like removed", null));
//    }
//
//    @PostMapping("/{id}/bookmark")
//    public ResponseEntity<ApiResponse<Void>> bookmarkArticle(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        articleService.bookmarkArticle(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Article bookmarked", null));
//    }
//
//    @DeleteMapping("/{id}/bookmark")
//    public ResponseEntity<ApiResponse<Void>> removeBookmark(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        articleService.removeBookmark(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Bookmark removed", null));
//    }
//
//    @PostMapping("/{id}/share")
//    public ResponseEntity<ApiResponse<Void>> shareArticle(
//            @PathVariable Long id,
//            @RequestParam String platform,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        articleService.shareArticle(id, userId, platform);
//        return ResponseEntity.ok(ApiResponse.success("Article shared", null));
//    }
//
//    @PostMapping("/{id}/view")
//    public ResponseEntity<ApiResponse<Void>> recordView(
//            @PathVariable Long id,
//            @RequestParam(required = false) Integer duration,
//            @RequestParam(required = false) Integer scrollDepth,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        articleService.recordView(id, userId, duration, scrollDepth);
//        return ResponseEntity.ok(ApiResponse.success("View recorded", null));
//    }
//
//    // ==================== STATUS CHECKS ====================
//
//    @GetMapping("/{id}/liked")
//    public ResponseEntity<ApiResponse<Boolean>> isArticleLiked(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.ok(ApiResponse.success("Not authenticated", false));
//        }
//        boolean liked = articleService.isArticleLikedByUser(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Like status checked", liked));
//    }
//
//    @GetMapping("/{id}/bookmarked")
//    public ResponseEntity<ApiResponse<Boolean>> isArticleBookmarked(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = getUserId(userDetails);
//        if (userId == null) {
//            return ResponseEntity.ok(ApiResponse.success("Not authenticated", false));
//        }
//        boolean bookmarked = articleService.isArticleBookmarkedByUser(id, userId);
//        return ResponseEntity.ok(ApiResponse.success("Bookmark status checked", bookmarked));
//    }
//}
