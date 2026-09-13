package com.example.youtubeextractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing extracted videos from the YouTube playlist")
public record PlaylistExtractResponse(
        @Schema(description = "Extracted YouTube playlist ID", example = "PLI7xEYXD8JT0")
        String playlistId,

        @Schema(description = "Total number of videos extracted", example = "2")
        int totalVideos,

        @Schema(description = "List of videos in the playlist")
        List<VideoDto> videos
) {
}
