package com.example.youtubeextractor.service;

import com.example.youtubeextractor.client.YouTubeApiClient;
import com.example.youtubeextractor.dto.PlaylistExtractResponse;
import com.example.youtubeextractor.dto.VideoDto;
import com.example.youtubeextractor.dto.youtube.YouTubePlaylistItem;
import com.example.youtubeextractor.dto.youtube.YouTubePlaylistItemListResponse;
import com.example.youtubeextractor.exception.EmptyPlaylistException;
import com.example.youtubeextractor.util.YouTubeUrlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubePlaylistService implements PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(YouTubePlaylistService.class);
    private static final String YOUTUBE_WATCH_BASE_URL = "https://www.youtube.com/watch?v=";

    private final YouTubeApiClient youTubeApiClient;

    public YouTubePlaylistService(YouTubeApiClient youTubeApiClient) {
        this.youTubeApiClient = youTubeApiClient;
    }

    @Override
    public PlaylistExtractResponse extractVideos(String playlistUrl) {
        String playlistId = YouTubeUrlParser.extractPlaylistId(playlistUrl);
        log.info("Starting video extraction for playlist ID: {}", playlistId);

        List<VideoDto> videos = new ArrayList<>();
        String pageToken = null;
        int pageCount = 0;

        do {
            pageCount++;
            log.debug("Fetching page {} for playlist {}", pageCount, playlistId);

            YouTubePlaylistItemListResponse pageResponse = youTubeApiClient.fetchPlaylistItemsPage(playlistId, pageToken);

            if (pageResponse != null && pageResponse.items() != null) {
                for (YouTubePlaylistItem item : pageResponse.items()) {
                    if (item.snippet() != null && item.snippet().resourceId() != null) {
                        String videoId = item.snippet().resourceId().videoId();
                        if (videoId != null && !videoId.trim().isEmpty()) {
                            int currentPosition = videos.size() + 1;
                            String title = item.snippet().title() != null ? item.snippet().title() : "Untitled Video";
                            String watchUrl = YOUTUBE_WATCH_BASE_URL + videoId;
                            videos.add(new VideoDto(currentPosition, videoId, title, watchUrl));
                        }
                    }
                }
                pageToken = pageResponse.nextPageToken();
            } else {
                pageToken = null;
            }

        } while (pageToken != null && !pageToken.trim().isEmpty());

        log.info("Finished extraction for playlist {}. Total videos found: {} across {} page(s)",
                playlistId, videos.size(), pageCount);

        if (videos.isEmpty()) {
            throw new EmptyPlaylistException("No videos were found in this playlist.");
        }

        return new PlaylistExtractResponse(playlistId, videos.size(), videos);
    }
}
