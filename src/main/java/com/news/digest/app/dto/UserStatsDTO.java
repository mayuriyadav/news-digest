package com.news.digest.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsDTO {
    private Long totalArticlesRead;
    private Long uniqueArticlesRead;
    private Long totalBookmarks;
    private Double averageReadTimeSeconds;
    private String topCategory;         // most read category
}
