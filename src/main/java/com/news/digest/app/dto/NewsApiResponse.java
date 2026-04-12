package com.news.digest.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.news.digest.app.model.NewsApiArticle;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiResponse {
    private String status;
    private Integer totalResults;
    private List<NewsApiArticle> articles;
}
