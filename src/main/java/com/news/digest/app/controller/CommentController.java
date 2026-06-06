package com.news.digest.app.controller;

import com.news.digest.app.dto.ApiResponse;
import com.news.digest.app.dto.CommentDTO;
import com.news.digest.app.dto.CommentRequest;
import com.news.digest.app.model.Article;
import com.news.digest.app.model.ArticleComment;
import com.news.digest.app.model.User;
import com.news.digest.app.repository.ArticleCommentRepository;
import com.news.digest.app.repository.ArticleRepository;
import com.news.digest.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;


/**
 * Comment endpoints:
 *  GET    /api/articles/{id}/comments              → get top-level comments (public)
 *  POST   /api/articles/{id}/comments              → add comment (auth required)
 *  GET    /api/articles/{id}/comments/{cId}/replies → get replies (public)
 *  POST   /api/articles/{id}/comments/{cId}/replies → reply to comment (auth required)
 *  PUT    /api/articles/{id}/comments/{cId}         → edit comment (auth required)
 *  DELETE /api/articles/{id}/comments/{cId}         → delete comment (auth required)
 *  POST   /api/articles/{id}/comments/{cId}/like    → like a comment (auth required)
 */
@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ArticleCommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    // ── Get top-level comments (public) ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CommentDTO> comments = commentRepository
                .findByArticleIdAndParentIsNullOrderByCreatedAtDesc(articleId, PageRequest.of(page, size))
                .map(this::toDTO);

        return ResponseEntity.ok(ApiResponse.success("Comments fetched", comments));
    }

    // ── Add a new comment (auth required) ────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @PathVariable Long articleId,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) return unauthorized();

        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null) return ResponseEntity.notFound().build();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return unauthorized();

        ArticleComment comment = new ArticleComment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setIsEdited(false);

        // Handle reply
        if (request.getParentId() != null) {
            ArticleComment parent = commentRepository.findById(request.getParentId()).orElse(null);
            if (parent == null) return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Parent comment not found"));
            comment.setParent(parent);
        }

        ArticleComment saved = commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", toDTO(saved)));
    }

    // ── Get replies to a comment (public) ────────────────────────────────────

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getReplies(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CommentDTO> replies = commentRepository
                .findByParentIdOrderByCreatedAtAsc(commentId, PageRequest.of(page, size))
                .map(this::toDTO);

        return ResponseEntity.ok(ApiResponse.success("Replies fetched", replies));
    }

    // ── Reply to a comment (auth required) ───────────────────────────────────

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentDTO>> replyToComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        request.setParentId(commentId);
        return addComment(articleId, request, httpRequest);
    }

    // ── Edit a comment (auth required, owner only) ───────────────────────────

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> editComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) return unauthorized();

        ArticleComment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.notFound().build();

        if (!comment.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only edit your own comments"));
        }

        comment.setContent(request.getContent());
        comment.setIsEdited(true);

        return ResponseEntity.ok(ApiResponse.success("Comment updated",
                toDTO(commentRepository.save(comment))));
    }

    // ── Delete a comment (auth required, owner only) ─────────────────────────

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) return unauthorized();

        ArticleComment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.notFound().build();

        if (!comment.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only delete your own comments"));
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    // ── Like a comment (auth required) ───────────────────────────────────────

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {

        Long userId = getUserId(httpRequest);
        if (userId == null) return unauthorized();

        if (!commentRepository.existsById(commentId)) return ResponseEntity.notFound().build();

        commentRepository.incrementLikeCount(commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment liked", null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CommentDTO toDTO(ArticleComment c) {
        CommentDTO dto = new CommentDTO();
        dto.setId(c.getId());
        dto.setArticleId(c.getArticle().getId());
        dto.setUserId(c.getUser().getId());
        dto.setUserName(c.getUser().getUserName());
        dto.setParentId(c.getParent() != null ? c.getParent().getId() : null);
        dto.setContent(c.getContent());
        dto.setLikeCount(c.getLikeCount());
        dto.setIsEdited(c.getIsEdited());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        // Include replies only for top-level comments
        if (c.getParent() == null && c.getReplies() != null && !c.getReplies().isEmpty()) {
            dto.setReplies(c.getReplies().stream().map(this::toDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }

    private <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
    }
}
