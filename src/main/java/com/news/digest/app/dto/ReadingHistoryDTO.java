package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryDTO {

    private Long id;
    private Long articleId;
    private String articleTitle;
    private String articleCategory;
    private LocalDateTime readAt;
    private Integer readDurationSeconds;
    private Integer scrollDepthPercentage;
    private Boolean isCompleted;
    private Integer readCount;
}
