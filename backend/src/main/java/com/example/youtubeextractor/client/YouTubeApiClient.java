package com.example.youtubeextractor.client;

import com.example.youtubeextractor.config.YouTubeConfig;
import com.example.youtubeextractor.dto.youtube.YouTubePlaylistItemListResponse;
import com.example.youtubeextractor.exception.InvalidPlaylistException;
import com.example.youtubeextractor.exception.PlaylistNotFoundException;
import com.example.youtubeextractor.exception.PlaylistPrivateException;
import com.example.youtubeextractor.exception.YouTubeApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class YouTubeApiClient {

    private static final Logger log = LoggerFactory.getLogger(YouTubeApiClient.class);

    private final RestClient youTubeRestClient;
    private final YouTubeConfig youTubeConfig;

    public YouTubeApiClient(RestClient youTubeRestClient, YouTubeConfig youTubeConfig) {
        this.youTubeRestClient = youTubeRestClient;
        this.youTubeConfig = youTubeConfig;
    }

    /**
     * Fetches a single page of playlist items from YouTube Data API v3.
     *
     * @param playlistId YouTube playlist ID
     * @param pageToken  Optional page token (null for first page)
     * @return YouTube playlist item response page
     */
    public YouTubePlaylistItemListResponse fetchPlaylistItemsPage(String playlistId, String pageToken) {
        if (!youTubeConfig.isApiKeyConfigured()) {
            throw new IllegalStateException("YouTube API key is not configured on the server. Please set the YOUTUBE_API_KEY environment variable.");
        }

        try {
            return youTubeRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/playlistItems")
                                .queryParam("part", "snippet")
                                .queryParam("maxResults", 50)
                                .queryParam("playlistId", playlistId)
                                .queryParam("key", youTubeConfig.getApiKey());

                        if (pageToken != null && !pageToken.trim().isEmpty()) {
                            uriBuilder.queryParam("pageToken", pageToken.trim());
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        int status = response.getStatusCode().value();
                        String responseBody = new String(response.getBody().readAllBytes());
                        log.warn("YouTube API returned status {}: {}", status, responseBody);

                        if (status == 404) {
                            throw new PlaylistNotFoundException("Playlist could not be found.");
                        } else if (status == 403) {
                            if (responseBody.contains("playlistNotFound") || responseBody.contains("accessForbidden") || responseBody.contains("forbidden")) {
                                throw new PlaylistPrivateException("This playlist is private or unavailable.");
                            }
                            throw new YouTubeApiException("Unable to retrieve playlist videos. Please try again.");
                        } else if (status == 400) {
                            if (responseBody.contains("API_KEY_INVALID") || responseBody.contains("API key not valid")) {
                                throw new IllegalStateException("The configured YouTube API key is invalid. Please verify your Google Cloud API key.");
                            }
                            throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
                        } else {
                            throw new YouTubeApiException("Unable to retrieve playlist videos. Please try again.");
                        }
                    })
                    .body(YouTubePlaylistItemListResponse.class);
        } catch (PlaylistNotFoundException | PlaylistPrivateException | InvalidPlaylistException | YouTubeApiException | IllegalStateException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("REST client error calling YouTube API: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            int status = ex.getStatusCode().value();
            String body = ex.getResponseBodyAsString();
            if (status == 404) {
                throw new PlaylistNotFoundException("Playlist could not be found.");
            } else if (status == 403) {
                if (body.contains("playlistNotFound") || body.contains("accessForbidden") || body.contains("forbidden")) {
                    throw new PlaylistPrivateException("This playlist is private or unavailable.");
                }
                throw new YouTubeApiException("Unable to retrieve playlist videos. Please try again.");
            } else if (status == 400) {
                if (body.contains("API_KEY_INVALID") || body.contains("API key not valid")) {
                    throw new IllegalStateException("The configured YouTube API key is invalid. Please verify your Google Cloud API key.");
                }
                throw new InvalidPlaylistException("Please enter a valid YouTube playlist URL.");
            }
            throw new YouTubeApiException("Unable to retrieve playlist videos. Please try again.", ex);
        } catch (Exception ex) {
            log.error("Unexpected error when calling YouTube API", ex);
            throw new YouTubeApiException("Unable to retrieve playlist videos. Please try again.", ex);
        }
    }
}
