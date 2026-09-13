package com.example.youtubeextractor.service;

import com.example.youtubeextractor.dto.PlaylistExtractResponse;

public interface PlaylistService {

    /**
     * Extracts all videos from the specified playlist URL, traversing all pages.
     *
     * @param playlistUrl YouTube playlist URL
     * @return playlist extraction response containing all extracted videos
     */
    PlaylistExtractResponse extractVideos(String playlistUrl);
}
