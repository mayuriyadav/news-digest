package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookmarkDTO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private String articleImageUrl;
    private String articleCategory;
    private String notes;
    private String folder;
    private LocalDateTime createdAt;
}
