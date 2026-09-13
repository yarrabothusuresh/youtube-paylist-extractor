package com.example.youtubeextractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for extracting playlist videos")
public record PlaylistExtractRequest(
        @NotBlank(message = "Playlist URL cannot be empty")
        @Schema(description = "YouTube playlist URL", example = "https://youtube.com/playlist?list=PLI7xEYXD8JT0")
        String playlistUrl
) {
}
