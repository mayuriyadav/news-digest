package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleDTO {

    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime fetchedAt;
    private String category;
    private Set<String> tags;
    private String language;
    private String country;
    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer shareCount;
    private Integer commentCount;
    private Integer readTimeMinutes;
    private Integer wordCount;
    private Double sentimentScore;
    private String sentimentLabel;
    private Double readabilityScore;
    private String featuredImage;
    private Boolean isFeatured;
    private Boolean isBreaking;
    private Boolean isPremium;
    private Boolean isActive;

    // User-specific fields (for personalized response)
    private boolean isBookmarkedByUser;
    private boolean isLikedByUser;
    private boolean isReadByUser;

}
