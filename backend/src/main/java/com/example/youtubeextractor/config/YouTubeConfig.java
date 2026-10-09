package com.example.youtubeextractor.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class YouTubeConfig {

    private static final Logger log = LoggerFactory.getLogger(YouTubeConfig.class);

    /**
     * Primary default file path for the YouTube API key.
     */
    public static final String DEFAULT_API_KEY_FILE_PATH = "C:\\dev\\Youtube_API.txt";

    @Value("${youtube.api.key:}")
    private String apiKey;

    @Value("${youtube.api.key-file:" + DEFAULT_API_KEY_FILE_PATH + "}")
    private String keyFilePath;

    @Value("${youtube.api.base-url:https://www.googleapis.com/youtube/v3}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        resolveApiKey();
    }

    private synchronized void resolveApiKey() {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.apiKey = sanitizeKey(apiKey);
            return;
        }

        // 1. Try configured file path if provided
        String targetPath = (keyFilePath != null && !keyFilePath.trim().isEmpty())
                ? keyFilePath.trim()
                : DEFAULT_API_KEY_FILE_PATH;

        if (tryLoadFromFile(targetPath)) {
            return;
        }

        // 2. Always fallback directly to C:\dev\Youtube_API.txt
        if (!DEFAULT_API_KEY_FILE_PATH.equalsIgnoreCase(targetPath)) {
            tryLoadFromFile(DEFAULT_API_KEY_FILE_PATH);
        }
    }

    private boolean tryLoadFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                String fileContent = Files.readString(path).trim();
                fileContent = sanitizeKey(fileContent);
                if (!fileContent.isEmpty()) {
                    this.apiKey = fileContent;
                    log.info("YouTube API key successfully loaded from file: {}", path);
                    return true;
                }
            } else {
                log.debug("YouTube API key file not found at: {}", path);
            }
        } catch (Exception e) {
            log.warn("Could not read YouTube API key from file {}: {}", filePath, e.getMessage());
        }
        return false;
    }

    private String sanitizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    public String getApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            resolveApiKey();
        }
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isApiKeyConfigured() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    @Bean
    public RestClient youTubeRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
