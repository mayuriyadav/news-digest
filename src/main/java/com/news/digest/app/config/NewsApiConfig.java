package com.news.digest.app.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class NewsApiConfig {

    @Value("${newsapi.base-url}")
    private String baseUrl;

    @Value("${newsapi.key}")
    private String apiKey;

    @Bean
    public WebClient newsApiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

}
