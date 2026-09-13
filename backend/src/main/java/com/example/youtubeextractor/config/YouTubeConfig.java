package com.example.youtubeextractor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class YouTubeConfig {

    @Value("${youtube.api.key:}")
    private String apiKey;

    @Value("${youtube.api.base-url:https://www.googleapis.com/youtube/v3}")
    private String baseUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Bean
    public RestClient youTubeRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
