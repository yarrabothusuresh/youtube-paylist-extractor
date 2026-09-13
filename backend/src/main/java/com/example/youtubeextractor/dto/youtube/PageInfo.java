package com.example.youtubeextractor.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageInfo(
        int totalResults,
        int resultsPerPage
) {
}
