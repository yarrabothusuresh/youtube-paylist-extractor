package com.example.youtubeextractor.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Snippet(
        String publishedAt,
        String channelId,
        String title,
        String description,
        int position,
        ResourceId resourceId
) {
}
