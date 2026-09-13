package com.example.youtubeextractor.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubePlaylistItem(
        String id,
        Snippet snippet
) {
}
