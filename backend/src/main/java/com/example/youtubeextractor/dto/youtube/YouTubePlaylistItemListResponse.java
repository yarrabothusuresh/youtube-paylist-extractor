package com.example.youtubeextractor.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubePlaylistItemListResponse(
        String kind,
        String etag,
        String nextPageToken,
        String prevPageToken,
        PageInfo pageInfo,
        List<YouTubePlaylistItem> items
) {
}
