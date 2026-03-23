package com.news.digest.app.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ArticleSearchDTO {
    private String keyword;
    private String category;
    private Long sourceId;
    private String author;
    private String language;
    private String country;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String sortBy = "publishedAt";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 20;
}

