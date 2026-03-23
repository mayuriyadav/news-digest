package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleRequestDTO {

    private String title;
    private String description;
    private String content;
    private String url;
    private String imageUrl;
    private String imageCaption;
    private String author;
    private Long sourceId;
    private String sourceName;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private String category;
    private Set<String> tags;
    private String language;
    private String country;
    private String featuredImage;
    private Boolean isFeatured;
    private Boolean isBreaking;
    private Boolean isPremium;
}
