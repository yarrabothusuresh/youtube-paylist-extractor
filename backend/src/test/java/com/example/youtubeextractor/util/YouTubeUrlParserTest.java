package com.example.youtubeextractor.util;

import com.example.youtubeextractor.exception.InvalidPlaylistException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YouTubeUrlParserTest {

    @Test
    @DisplayName("Should extract playlist ID from standard playlist URL")
    void shouldExtractFromStandardPlaylistUrl() {
        String url = "https://www.youtube.com/playlist?list=PLI7xEYXD8JT0";
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId(url));
    }

    @Test
    @DisplayName("Should extract playlist ID from youtube.com without www")
    void shouldExtractFromNonWwwPlaylistUrl() {
        String url = "https://youtube.com/playlist?list=PLI7xEYXD8JT0";
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId(url));
    }

    @Test
    @DisplayName("Should extract playlist ID from watch URL with list parameter")
    void shouldExtractFromWatchUrlWithList() {
        String url = "https://www.youtube.com/watch?v=VIDEO_ID&list=PLI7xEYXD8JT0";
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId(url));
    }

    @Test
    @DisplayName("Should extract playlist ID when list parameter comes before v parameter")
    void shouldExtractWhenListParamComesFirst() {
        String url = "https://www.youtube.com/watch?list=PLI7xEYXD8JT0&v=VIDEO_ID";
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId(url));
    }

    @Test
    @DisplayName("Should extract playlist ID with additional query parameters")
    void shouldExtractWithAdditionalQueryParams() {
        String url = "https://www.youtube.com/watch?v=abc123xyz&list=PLI7xEYXD8JT0&index=5&t=45s&si=randomString123";
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId(url));
    }

    @Test
    @DisplayName("Should extract playlist ID from relative playlist?list= format")
    void shouldExtractFromRelativePlaylistQuery() {
        String query = "playlist?list=ABC123";
        assertEquals("ABC123", YouTubeUrlParser.extractPlaylistId(query));
    }

    @Test
    @DisplayName("Should extract playlist ID from relative watch?v=xyz&list= format")
    void shouldExtractFromRelativeWatchQuery() {
        String query = "watch?v=xyz&list=ABC123";
        assertEquals("ABC123", YouTubeUrlParser.extractPlaylistId(query));
    }

    @Test
    @DisplayName("Should extract playlist ID from mobile and music YouTube URLs")
    void shouldExtractFromMobileAndMusicUrls() {
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId("https://m.youtube.com/playlist?list=PLI7xEYXD8JT0"));
        assertEquals("PLI7xEYXD8JT0", YouTubeUrlParser.extractPlaylistId("https://music.youtube.com/playlist?list=PLI7xEYXD8JT0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "https://vimeo.com/playlist?list=PLI7xEYXD8JT0",
            "https://www.google.com/search?q=youtube",
            "https://www.youtube.com/watch?v=VIDEO_ONLY",
            "https://www.youtube.com/",
            "not-a-valid-url"
    })
    @DisplayName("Should throw InvalidPlaylistException for invalid URLs")
    void shouldThrowForInvalidUrls(String invalidUrl) {
        InvalidPlaylistException ex = assertThrows(InvalidPlaylistException.class, () ->
                YouTubeUrlParser.extractPlaylistId(invalidUrl)
        );
        assertEquals("Please enter a valid YouTube playlist URL.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw InvalidPlaylistException for null URL")
    void shouldThrowForNullUrl() {
        InvalidPlaylistException ex = assertThrows(InvalidPlaylistException.class, () ->
                YouTubeUrlParser.extractPlaylistId(null)
        );
        assertEquals("Please enter a valid YouTube playlist URL.", ex.getMessage());
    }
}
