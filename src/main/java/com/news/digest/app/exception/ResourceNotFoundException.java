package com.news.digest.app.exception;

import org.springframework.http.HttpStatus;



public class ResourceNotFoundException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public ResourceNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
        this.message = message;
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
        this.status = HttpStatus.NOT_FOUND;
        this.message = String.format("%s not found with %s: '%s'", resource, field, value);
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    // ADD THESE STATIC METHODS - THIS FIXES YOUR ERRORS
    public static ResourceNotFoundException forArticleById(Long id) {
        return new ResourceNotFoundException("Article", "id", id);
    }

    public static ResourceNotFoundException forUserById(Long id) {
        return new ResourceNotFoundException("User", "id", id);
    }

    public static ResourceNotFoundException forSourceById(Long id) {
        return new ResourceNotFoundException("NewsSource", "id", id);
    }

    public static ResourceNotFoundException forBookmark(Long userId, Long articleId) {
        return new ResourceNotFoundException("Bookmark", "userId: " + userId + " and articleId: " + articleId, "not found");
    }

    public static ResourceNotFoundException forLike(Long userId, Long articleId) {
        return new ResourceNotFoundException("Like", "userId: " + userId + " and articleId: " + articleId, "not found");
    }
}
