package com.news.digest.app.service.impl;

import com.news.digest.app.dto.ArticleDTO;
import com.news.digest.app.dto.ArticleRequestDTO;
import com.news.digest.app.dto.ArticleSearchDTO;
import com.news.digest.app.exception.BadRequestException;
import com.news.digest.app.exception.ResourceNotFoundException;
import com.news.digest.app.model.*;
import com.news.digest.app.repository.*;
import com.news.digest.app.service.ArticleService;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ReadingHistoryRepository readingHistoryRepository;

    @Override
    @Transactional
    public ArticleDTO createArticle(ArticleRequestDTO request) {
        log.debug("Creating new article: {}", request.getTitle());

        // Check if article already exists by URL
        if (articleRepository.existsByUrl(request.getUrl())) {
            throw new BadRequestException("Article with URL " + request.getUrl() + " already exists");
        }

        Article article = new Article();
        mapRequestToEntity(request, article);

        // Set default values
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setFetchedAt(LocalDateTime.now());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setBookmarkCount(0);
        article.setShareCount(0);
        article.setCommentCount(0);
        article.setIsActive(true);

        // Calculate word count and read time
        article.calculateWordCount();
        article.calculateReadTime();

        Article savedArticle = articleRepository.save(article);
        log.info("Article created successfully with id: {}", savedArticle.getId());

        return convertToDTO(savedArticle, null);
    }

    @Override
    @Transactional
    public ArticleDTO getArticleById(Long id, Long userId) {
        log.debug("Fetching article by id: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(id));

        // Increment view count
        article.incrementViewCount();
        articleRepository.save(article);

        // Record view if user is logged in
        if (userId != null) {
            recordView(id, userId, null, null);
        }

        return convertToDTO(article, userId);
    }

    @Override
    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleRequestDTO request) {
        log.debug("Updating article with id: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(id));

        mapRequestToEntity(request, article);
        article.setUpdatedAt(LocalDateTime.now());

        // Recalculate word count and read time if content changed
        if (request.getContent() != null) {
            article.calculateWordCount();
            article.calculateReadTime();
        }

        Article updatedArticle = articleRepository.save(article);
        log.info("Article updated successfully with id: {}", updatedArticle.getId());

        return convertToDTO(updatedArticle, null);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        log.debug("Deleting article with id: {}", id);

        if (!articleRepository.existsById(id)) {
            throw ResourceNotFoundException.forArticleById(id);
        }

        articleRepository.deleteById(id);
        log.info("Article deleted successfully with id: {}", id);
    }

    @Override
    public Page<ArticleDTO> getAllArticles(int page, int size, String sortBy, String sortDir, Long userId) {
        log.debug("Fetching all articles - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesByCategory(String category, int page, int size, Long userId) {
        log.debug("Fetching articles by category: {}", category);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByCategory(category, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesBySource(Long sourceId, int page, int size, Long userId) {
        log.debug("Fetching articles by source: {}", sourceId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findBySourceId(sourceId, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesByAuthor(String author, int page, int size, Long userId) {
        log.debug("Fetching articles by author: {}", author);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByAuthor(author, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> searchArticles(String keyword, int page, int size, Long userId) {
        log.debug("Searching articles with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllArticles(page, size, "publishedAt", "desc", userId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.searchArticles(keyword, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> advancedSearch(ArticleSearchDTO searchDTO, Long userId) {
        log.debug("Advanced search with criteria: {}", searchDTO);

        Pageable pageable = createPageable(searchDTO);

        Page<Article> articles = articleRepository.advancedSearch(
                searchDTO.getKeyword(),
                searchDTO.getCategory(),
                searchDTO.getSourceId(),
                searchDTO.getAuthor(),
                searchDTO.getLanguage(),
                searchDTO.getCountry(),
                searchDTO.getStartDate(),
                searchDTO.getEndDate(),
                pageable
        );

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getTrendingArticles(int page, int size, Long userId) {
        log.debug("Fetching trending articles");

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articles = articleRepository.findTrendingArticles(oneWeekAgo, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getMostViewedArticles(int page, int size, Long userId) {
        log.debug("Fetching most viewed articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("viewCount").descending());
        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getMostLikedArticles(int page, int size, Long userId) {
        log.debug("Fetching most liked articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("likeCount").descending());
        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getMostBookmarkedArticles(int page, int size, Long userId) {
        log.debug("Fetching most bookmarked articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("bookmarkCount").descending());
        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getLatestArticles(int page, int size, Long userId) {
        log.debug("Fetching latest articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getFeaturedArticles(int page, int size, Long userId) {
        log.debug("Fetching featured articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByIsFeaturedTrue(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getBreakingNews(int page, int size, Long userId) {
        log.debug("Fetching breaking news");

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByIsBreakingTrue(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getPremiumArticles(int page, int size, Long userId) {
        log.debug("Fetching premium articles");

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByIsPremiumTrue(pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesByLanguage(String language, int page, int size, Long userId) {
        log.debug("Fetching articles by language: {}", language);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByLanguage(language, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesByCountry(String country, int page, int size, Long userId) {
        log.debug("Fetching articles by country: {}", country);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByCountry(country, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public Page<ArticleDTO> getArticlesByDateRange(LocalDateTime start, LocalDateTime end, int page, int size, Long userId) {
        log.debug("Fetching articles between {} and {}", start, end);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articles = articleRepository.findByPublishedAtBetween(start, end, pageable);

        return articles.map(article -> convertToDTO(article, userId));
    }

    @Override
    public List<String> getAllCategories() {
        log.debug("Fetching all categories");
        return articleRepository.findAllCategories();
    }

    @Override
    public List<String> getAllSources() {
        log.debug("Fetching all sources");
        return articleRepository.findAllSourceNames();
    }

    @Override
    public List<String> getAllLanguages() {
        log.debug("Fetching all languages");
        return articleRepository.findAllLanguages();
    }

    @Override
    public List<String> getAllCountries() {
        log.debug("Fetching all countries");
        return articleRepository.findAllCountries();
    }

    @Override
    public Map<String, Object> getArticleStatistics() {
        log.debug("Fetching article statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", articleRepository.count());
        stats.put("categories", getCategoryWiseCount());
        stats.put("sources", getSourceWiseCount());
        stats.put("languages", getLanguageWiseCount());

        return stats;
    }

    @Override
    public Map<String, Long> getCategoryWiseCount() {
        return articleRepository.countArticlesByCategory().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public Map<String, Long> getSourceWiseCount() {
        return articleRepository.countArticlesBySource().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public Map<String, Long> getLanguageWiseCount() {
        return articleRepository.countArticlesByLanguage().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    @Transactional
    public void likeArticle(Long articleId, Long userId) {
        log.debug("User {} liking article {}", userId, articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forUserById(userId));

        // Check if already liked
        if (articleLikeRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new BadRequestException("Article already liked");
        }

        ArticleLike like = new ArticleLike();
        like.setUser(user);
        like.setArticle(article);
        like.setLikedAt(LocalDateTime.now());

        articleLikeRepository.save(like);
        article.incrementLikeCount();
        articleRepository.save(article);

        log.info("Article {} liked by user {}", articleId, userId);
    }

    @Override
    @Transactional
    public void unlikeArticle(Long articleId, Long userId) {
        log.debug("User {} unliking article {}", userId, articleId);

        ArticleLike like = articleLikeRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        Article article = like.getArticle();

        articleLikeRepository.delete(like);
        article.decrementLikeCount();
        articleRepository.save(article);

        log.info("Article {} unliked by user {}", articleId, userId);
    }

    @Override
    @Transactional
    public void bookmarkArticle(Long articleId, Long userId) {
        log.debug("User {} bookmarking article {}", userId, articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(articleId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forUserById(userId));

        // Check if already bookmarked
        if (bookmarkRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new BadRequestException("Article already bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setArticle(article);
        bookmark.setCreatedAt(LocalDateTime.now());

        bookmarkRepository.save(bookmark);
        article.incrementBookmarkCount();
        articleRepository.save(article);

        log.info("Article {} bookmarked by user {}", articleId, userId);
    }

    @Override
    @Transactional
    public void removeBookmark(Long articleId, Long userId) {
        log.debug("User {} removing bookmark for article {}", userId, articleId);

        Bookmark bookmark = bookmarkRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        Article article = bookmark.getArticle();

        bookmarkRepository.delete(bookmark);
        article.decrementBookmarkCount();
        articleRepository.save(article);

        log.info("Bookmark removed for article {} by user {}", articleId, userId);
    }

    @Override
    @Transactional
    public void shareArticle(Long articleId, Long userId, String platform) {
        log.debug("User {} sharing article {} on {}", userId, articleId, platform);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(articleId));

        article.incrementShareCount();
        articleRepository.save(article);

        // You can save share details in a separate table if needed

        log.info("Article {} shared on {} by user {}", articleId, platform, userId);
    }

    @Override
    @Transactional
    public void recordView(Long articleId, Long userId, Integer readDuration, Integer scrollDepth) {
        if (userId == null) return;

        log.debug("Recording view for article {} by user {}", articleId, userId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.forArticleById(articleId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forUserById(userId));

        // Check if already in history
        Optional<ReadingHistory> existingHistory = readingHistoryRepository
                .findByUserIdAndArticleId(userId, articleId);

        if (existingHistory.isPresent()) {
            // Update existing record
            ReadingHistory history = existingHistory.get();
            history.setReadCount(history.getReadCount() + 1);
            if (readDuration != null) history.setReadDurationSeconds(readDuration);
            if (scrollDepth != null) history.setScrollDepthPercentage(scrollDepth);
            history.setReadAt(LocalDateTime.now());
            readingHistoryRepository.save(history);
        } else {
            // Create new record
            ReadingHistory history = new ReadingHistory();
            history.setUser(user);
            history.setArticle(article);
            history.setReadAt(LocalDateTime.now());
            history.setReadCount(1);
            if (readDuration != null) history.setReadDurationSeconds(readDuration);
            if (scrollDepth != null) history.setScrollDepthPercentage(scrollDepth);
            readingHistoryRepository.save(history);
        }

        log.debug("View recorded for article {} by user {}", articleId, userId);
    }

    @Override
    public boolean isArticleLikedByUser(Long articleId, Long userId) {
        if (userId == null) return false;
        return articleLikeRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    @Override
    public boolean isArticleBookmarkedByUser(Long articleId, Long userId) {
        if (userId == null) return false;
        return bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    @Override
    public boolean isArticleReadByUser(Long articleId, Long userId) {
        if (userId == null) return false;
        return readingHistoryRepository.findByUserIdAndArticleId(userId, articleId).isPresent();
    }

    // ==================== HELPER METHODS ====================

    private void mapRequestToEntity(ArticleRequestDTO request, Article article) {
        article.setTitle(request.getTitle());
        article.setDescription(request.getDescription());
        article.setContent(request.getContent());
        article.setUrl(request.getUrl());
        article.setImageUrl(request.getImageUrl());
        article.setImageCaption(request.getImageCaption());
        article.setAuthor(request.getAuthor());
        article.setSourceId(request.getSourceId());
        article.setSourceName(request.getSourceName());
        article.setSourceUrl(request.getSourceUrl());
        article.setPublishedAt(request.getPublishedAt());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setLanguage(request.getLanguage());
        article.setCountry(request.getCountry());
        article.setFeaturedImage(request.getFeaturedImage());
        article.setIsFeatured(request.getIsFeatured());
        article.setIsBreaking(request.getIsBreaking());
        article.setIsPremium(request.getIsPremium());
    }

    private Pageable createPageable(ArticleSearchDTO searchDTO) {
        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(searchDTO.getSortBy()).descending()
                : Sort.by(searchDTO.getSortBy()).ascending();

        return PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
    }

    private ArticleDTO convertToDTO(Article article, Long userId) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setDescription(article.getDescription());
        dto.setContent(article.getContent());
        dto.setUrl(article.getUrl());
        dto.setImageUrl(article.getImageUrl());
        dto.setImageCaption(article.getImageCaption());
        dto.setAuthor(article.getAuthor());
        dto.setSourceId(article.getSourceId());
        dto.setSourceName(article.getSourceName());
        dto.setSourceUrl(article.getSourceUrl());
        dto.setPublishedAt(article.getPublishedAt());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        dto.setFetchedAt(article.getFetchedAt());
        dto.setCategory(article.getCategory());
        dto.setTags(article.getTags());
        dto.setLanguage(article.getLanguage());
        dto.setCountry(article.getCountry());
        dto.setViewCount(article.getViewCount());
        dto.setLikeCount(article.getLikeCount());
        dto.setBookmarkCount(article.getBookmarkCount());
        dto.setShareCount(article.getShareCount());
        dto.setCommentCount(article.getCommentCount());
        dto.setReadTimeMinutes(article.getReadTimeMinutes());
        dto.setWordCount(article.getWordCount());
        dto.setSentimentScore(article.getSentimentScore());
        dto.setSentimentLabel(article.getSentimentLabel());
        dto.setReadabilityScore(article.getReadabilityScore());
        dto.setFeaturedImage(article.getFeaturedImage());
        dto.setIsFeatured(article.getIsFeatured());
        dto.setIsBreaking(article.getIsBreaking());
        dto.setIsPremium(article.getIsPremium());
        dto.setIsActive(article.getIsActive());

        // Set user-specific flags if userId is provided
        if (userId != null) {
            dto.setBookmarkedByUser(isArticleBookmarkedByUser(article.getId(), userId));
            dto.setLikedByUser(isArticleLikedByUser(article.getId(), userId));
            dto.setReadByUser(isArticleReadByUser(article.getId(), userId));
        }

        return dto;
    }
}
