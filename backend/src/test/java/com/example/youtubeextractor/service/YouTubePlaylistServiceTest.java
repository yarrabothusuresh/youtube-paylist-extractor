package com.example.youtubeextractor.service;

import com.example.youtubeextractor.client.YouTubeApiClient;
import com.example.youtubeextractor.dto.PlaylistExtractResponse;
import com.example.youtubeextractor.dto.youtube.PageInfo;
import com.example.youtubeextractor.dto.youtube.ResourceId;
import com.example.youtubeextractor.dto.youtube.Snippet;
import com.example.youtubeextractor.dto.youtube.YouTubePlaylistItem;
import com.example.youtubeextractor.dto.youtube.YouTubePlaylistItemListResponse;
import com.example.youtubeextractor.exception.EmptyPlaylistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YouTubePlaylistServiceTest {

    @Mock
    private YouTubeApiClient youTubeApiClient;

    private YouTubePlaylistService playlistService;

    @BeforeEach
    void setUp() {
        playlistService = new YouTubePlaylistService(youTubeApiClient);
    }

    private YouTubePlaylistItem createPlaylistItem(String videoId, String title, int position) {
        Snippet snippet = new Snippet(
                "2026-01-01T00:00:00Z",
                "channel-123",
                title,
                "Description for " + title,
                position,
                new ResourceId("youtube#video", videoId)
        );
        return new YouTubePlaylistItem("item-" + videoId, snippet);
    }

    private List<YouTubePlaylistItem> createItems(int startIdx, int count) {
        List<YouTubePlaylistItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int num = startIdx + i;
            items.add(createPlaylistItem("video_" + num, "Video Title " + num, i));
        }
        return items;
    }

    @Test
    @DisplayName("Should retrieve all 125 videos across 3 paginated pages")
    void shouldExtractAllVideosAcrossThreePages() {
        String playlistId = "PLTestPlaylist123";
        String playlistUrl = "https://www.youtube.com/playlist?list=" + playlistId;

        // Page 1: 50 items, nextPageToken = "page2"
        List<YouTubePlaylistItem> page1Items = createItems(1, 50);
        YouTubePlaylistItemListResponse page1Response = new YouTubePlaylistItemListResponse(
                "youtube#playlistItemListResponse", "etag1", "page2", null, new PageInfo(125, 50), page1Items
        );

        // Page 2: 50 items, nextPageToken = "page3"
        List<YouTubePlaylistItem> page2Items = createItems(51, 50);
        YouTubePlaylistItemListResponse page2Response = new YouTubePlaylistItemListResponse(
                "youtube#playlistItemListResponse", "etag2", "page3", "page1", new PageInfo(125, 50), page2Items
        );

        // Page 3: 25 items, nextPageToken = null
        List<YouTubePlaylistItem> page3Items = createItems(101, 25);
        YouTubePlaylistItemListResponse page3Response = new YouTubePlaylistItemListResponse(
                "youtube#playlistItemListResponse", "etag3", null, "page2", new PageInfo(125, 25), page3Items
        );

        when(youTubeApiClient.fetchPlaylistItemsPage(playlistId, null)).thenReturn(page1Response);
        when(youTubeApiClient.fetchPlaylistItemsPage(playlistId, "page2")).thenReturn(page2Response);
        when(youTubeApiClient.fetchPlaylistItemsPage(playlistId, "page3")).thenReturn(page3Response);

        PlaylistExtractResponse result = playlistService.extractVideos(playlistUrl);

        assertNotNull(result);
        assertEquals(playlistId, result.playlistId());
        assertEquals(125, result.totalVideos());
        assertEquals(125, result.videos().size());

        // Verify ordering and URL structure
        assertEquals(1, result.videos().getFirst().position());
        assertEquals("video_1", result.videos().getFirst().videoId());
        assertEquals("https://www.youtube.com/watch?v=video_1", result.videos().getFirst().url());

        assertEquals(125, result.videos().get(124).position());
        assertEquals("video_125", result.videos().get(124).videoId());
        assertEquals("https://www.youtube.com/watch?v=video_125", result.videos().get(124).url());

        verify(youTubeApiClient, times(1)).fetchPlaylistItemsPage(playlistId, null);
        verify(youTubeApiClient, times(1)).fetchPlaylistItemsPage(playlistId, "page2");
        verify(youTubeApiClient, times(1)).fetchPlaylistItemsPage(playlistId, "page3");
    }

    @Test
    @DisplayName("Should throw EmptyPlaylistException when playlist has no items")
    void shouldThrowWhenPlaylistIsEmpty() {
        String playlistId = "PLEmptyPlaylist";
        String playlistUrl = "https://www.youtube.com/playlist?list=" + playlistId;

        YouTubePlaylistItemListResponse emptyResponse = new YouTubePlaylistItemListResponse(
                "youtube#playlistItemListResponse", "etag", null, null, new PageInfo(0, 50), Collections.emptyList()
        );

        when(youTubeApiClient.fetchPlaylistItemsPage(playlistId, null)).thenReturn(emptyResponse);

        EmptyPlaylistException ex = assertThrows(EmptyPlaylistException.class, () ->
                playlistService.extractVideos(playlistUrl)
        );

        assertEquals("No videos were found in this playlist.", ex.getMessage());
    }
}
