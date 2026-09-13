package com.example.youtubeextractor.util;

import com.example.youtubeextractor.exception.InvalidPlaylistException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YouTubeUrlParser {

    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{2,100}$");
    private static final Pattern LIST_PARAM_REGEX = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)");

    private YouTubeUrlParser() {
        // Utility class
    }

    /**
     * Extracts and validates a YouTube playlist ID from a given URL string.
     *
     * @param rawUrl the YouTube URL entered by user
     * @return validated playlist ID
     * @throws InvalidPlaylistException if URL is null, malformed, not YouTube, or missing valid playlist ID
     */
    public static String extractPlaylistId(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
        }

        String trimmed = rawUrl.trim();

        // Check if user passed full URL or relative query
        String host = null;
        try {
            URI uri = URI.create(trimmed);
            host = uri.getHost();
        } catch (Exception ignored) {
            // Might not have protocol scheme yet or could be malformed
        }

        if (host != null) {
            String lowerHost = host.toLowerCase();
            boolean isYouTubeHost = lowerHost.equals("youtube.com")
                    || lowerHost.endsWith(".youtube.com")
                    || lowerHost.equals("youtu.be");

            if (!isYouTubeHost) {
                throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
            }
        } else if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            // Also accept formats like "playlist?list=..." or "watch?v=...&list=..."
            if (!trimmed.startsWith("playlist?") && !trimmed.startsWith("watch?") && !trimmed.contains("list=")) {
                throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
            }
        }

        // Extract the list parameter using regex first
        Matcher matcher = LIST_PARAM_REGEX.matcher(trimmed);
        if (matcher.find()) {
            String candidateId = matcher.group(1);
            if (candidateId != null && PLAYLIST_ID_PATTERN.matcher(candidateId).matches()) {
                return candidateId;
            }
        }

        // Fallback: parse URI query string
        try {
            String query = null;
            if (trimmed.contains("?")) {
                query = trimmed.substring(trimmed.indexOf('?') + 1);
            }
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        if ("list".equalsIgnoreCase(key) && PLAYLIST_ID_PATTERN.matcher(value).matches()) {
                            return value;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallthrough to exception
        }

        throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
    }
}
