package com.news.digest.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiArticle {

    private NewsApiSource source;
    private String author;
    private String title;
    private String description;
    private String url;

    @JsonProperty("urlToImage")
    private String urlToImage;

    @JsonProperty("publishedAt")
    private OffsetDateTime publishedAt;

    private String content;
}
