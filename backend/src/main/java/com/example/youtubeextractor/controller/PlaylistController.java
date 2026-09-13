package com.example.youtubeextractor.controller;

import com.example.youtubeextractor.dto.PlaylistExtractRequest;
import com.example.youtubeextractor.dto.PlaylistExtractResponse;
import com.example.youtubeextractor.exception.ErrorResponse;
import com.example.youtubeextractor.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
@Tag(name = "Playlist", description = "YouTube Playlist extraction endpoints")
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/extract")
    @Operation(
            summary = "Extract all video links from a YouTube playlist",
            description = "Extracts all video URLs from a given YouTube playlist URL with automatic pagination handling."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully extracted playlist videos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaylistExtractResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    value = """
                                            {
                                              "playlistId": "PLI7xEYXD8JT0",
                                              "totalVideos": 2,
                                              "videos": [
                                                {
                                                  "position": 1,
                                                  "videoId": "abc123xyz",
                                                  "title": "Introduction to Spring Boot 3",
                                                  "url": "https://www.youtube.com/watch?v=abc123xyz"
                                                },
                                                {
                                                  "position": 2,
                                                  "videoId": "def456uvw",
                                                  "title": "Spring Boot REST APIs with Java 21",
                                                  "url": "https://www.youtube.com/watch?v=def456uvw"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid playlist URL or format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-09-12T21:00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Please enter a valid YouTube playlist URL."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Playlist is private or unavailable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-09-12T21:00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "This playlist is private or unavailable."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist not found or empty",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-09-12T21:00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Playlist could not be found."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server or configuration error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "YouTube API communication error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PlaylistExtractResponse> extractPlaylistVideos(
            @Valid @RequestBody PlaylistExtractRequest request
    ) {
        log.info("Received request to extract playlist: {}", request.playlistUrl());
        PlaylistExtractResponse response = playlistService.extractVideos(request.playlistUrl());
        return ResponseEntity.ok(response);
    }
}
