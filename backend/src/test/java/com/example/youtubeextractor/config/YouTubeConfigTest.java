package com.example.youtubeextractor.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YouTubeConfigTest {

    @Test
    void shouldLoadApiKeyFromFileWhenPropertyIsEmpty(@TempDir Path tempDir) throws IOException {
        Path apiKeyFile = tempDir.resolve("Youtube_API.txt");
        Files.writeString(apiKeyFile, "AIzaSyTestApiKeyFromTempFile\n");

        YouTubeConfig config = new YouTubeConfig();
        ReflectionTestUtils.setField(config, "apiKey", "");
        ReflectionTestUtils.setField(config, "keyFilePath", apiKeyFile.toString());

        config.init();

        assertTrue(config.isApiKeyConfigured());
        assertEquals("AIzaSyTestApiKeyFromTempFile", config.getApiKey());
    }

    @Test
    void shouldPreferConfiguredApiKeyOverFile(@TempDir Path tempDir) throws IOException {
        Path apiKeyFile = tempDir.resolve("Youtube_API.txt");
        Files.writeString(apiKeyFile, "FileKey");

        YouTubeConfig config = new YouTubeConfig();
        ReflectionTestUtils.setField(config, "apiKey", "EnvVarKey");
        ReflectionTestUtils.setField(config, "keyFilePath", apiKeyFile.toString());

        config.init();

        assertTrue(config.isApiKeyConfigured());
        assertEquals("EnvVarKey", config.getApiKey());
    }

    @Test
    void shouldSanitizeQuotesAndWhitespaceFromFile(@TempDir Path tempDir) throws IOException {
        Path apiKeyFile = tempDir.resolve("Youtube_API.txt");
        Files.writeString(apiKeyFile, "  \"AIzaSyQuotedKey\"  \r\n");

        YouTubeConfig config = new YouTubeConfig();
        ReflectionTestUtils.setField(config, "apiKey", "");
        ReflectionTestUtils.setField(config, "keyFilePath", apiKeyFile.toString());

        config.init();

        assertTrue(config.isApiKeyConfigured());
        assertEquals("AIzaSyQuotedKey", config.getApiKey());
    }
}
