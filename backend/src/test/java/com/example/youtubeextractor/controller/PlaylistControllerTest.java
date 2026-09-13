package com.example.youtubeextractor.controller;

import com.example.youtubeextractor.dto.PlaylistExtractRequest;
import com.example.youtubeextractor.dto.PlaylistExtractResponse;
import com.example.youtubeextractor.dto.VideoDto;
import com.example.youtubeextractor.exception.GlobalExceptionHandler;
import com.example.youtubeextractor.exception.InvalidPlaylistException;
import com.example.youtubeextractor.exception.PlaylistNotFoundException;
import com.example.youtubeextractor.exception.PlaylistPrivateException;
import com.example.youtubeextractor.service.PlaylistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaylistController.class)
@Import(GlobalExceptionHandler.class)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaylistService playlistService;

    @Test
    @DisplayName("POST /api/playlists/extract should return 200 with extracted videos")
    void shouldReturn200WithExtractedVideos() throws Exception {
        String playlistUrl = "https://youtube.com/playlist?list=PLI7xEYXD8JT0";
        PlaylistExtractRequest request = new PlaylistExtractRequest(playlistUrl);

        List<VideoDto> videos = List.of(
                new VideoDto(1, "abc123", "Video 1", "https://www.youtube.com/watch?v=abc123"),
                new VideoDto(2, "xyz456", "Video 2", "https://www.youtube.com/watch?v=xyz456")
        );
        PlaylistExtractResponse response = new PlaylistExtractResponse("PLI7xEYXD8JT0", 2, videos);

        when(playlistService.extractVideos(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/playlists/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playlistId").value("PLI7xEYXD8JT0"))
                .andExpect(jsonPath("$.totalVideos").value(2))
                .andExpect(jsonPath("$.videos[0].position").value(1))
                .andExpect(jsonPath("$.videos[0].videoId").value("abc123"))
                .andExpect(jsonPath("$.videos[0].title").value("Video 1"))
                .andExpect(jsonPath("$.videos[0].url").value("https://www.youtube.com/watch?v=abc123"))
                .andExpect(jsonPath("$.videos[1].position").value(2))
                .andExpect(jsonPath("$.videos[1].videoId").value("xyz456"))
                .andExpect(jsonPath("$.videos[1].url").value("https://www.youtube.com/watch?v=xyz456"));
    }

    @Test
    @DisplayName("POST /api/playlists/extract should return 400 when URL is invalid")
    void shouldReturn400WhenInvalidUrl() throws Exception {
        PlaylistExtractRequest request = new PlaylistExtractRequest("https://invalid-url.com/something");

        when(playlistService.extractVideos(anyString()))
                .thenThrow(new InvalidPlaylistException("Please enter a valid YouTube playlist URL."));

        mockMvc.perform(post("/api/playlists/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Please enter a valid YouTube playlist URL."));
    }

    @Test
    @DisplayName("POST /api/playlists/extract should return 404 when playlist is not found")
    void shouldReturn404WhenNotFound() throws Exception {
        PlaylistExtractRequest request = new PlaylistExtractRequest("https://youtube.com/playlist?list=PLDoesNotExist");

        when(playlistService.extractVideos(anyString()))
                .thenThrow(new PlaylistNotFoundException("Playlist could not be found."));

        mockMvc.perform(post("/api/playlists/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Playlist could not be found."));
    }

    @Test
    @DisplayName("POST /api/playlists/extract should return 403 when playlist is private")
    void shouldReturn403WhenPrivate() throws Exception {
        PlaylistExtractRequest request = new PlaylistExtractRequest("https://youtube.com/playlist?list=PLPrivate123");

        when(playlistService.extractVideos(anyString()))
                .thenThrow(new PlaylistPrivateException("This playlist is private or unavailable."));

        mockMvc.perform(post("/api/playlists/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("This playlist is private or unavailable."));
    }

    @Test
    @DisplayName("POST /api/playlists/extract should return 400 when request body has blank URL")
    void shouldReturn400WhenBlankUrl() throws Exception {
        PlaylistExtractRequest request = new PlaylistExtractRequest("   ");

        mockMvc.perform(post("/api/playlists/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Please enter a valid YouTube playlist URL."));
    }
}
