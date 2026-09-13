package com.example.youtubeextractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Video information extracted from YouTube playlist")
public record VideoDto(
        @Schema(description = "Position in the playlist (1-indexed)", example = "1")
        int position,

        @Schema(description = "YouTube video ID", example = "abc123xyz")
        String videoId,

        @Schema(description = "Title of the video", example = "Sample Video Title")
        String title,

        @Schema(description = "Full YouTube watch URL", example = "https://www.youtube.com/watch?v=abc123xyz")
        String url
) {
}
