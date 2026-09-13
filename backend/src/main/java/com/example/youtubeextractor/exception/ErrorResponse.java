package com.example.youtubeextractor.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message);
    }
}
