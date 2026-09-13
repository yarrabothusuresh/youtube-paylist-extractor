package com.example.youtubeextractor.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidPlaylistException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlaylistException(InvalidPlaylistException ex) {
        log.warn("Invalid playlist requested: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error on request body: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Please enter a valid YouTube playlist URL."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON payload: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Please enter a valid YouTube playlist URL."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistNotFoundException(PlaylistNotFoundException ex) {
        log.warn("Playlist not found: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(PlaylistPrivateException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistPrivateException(PlaylistPrivateException ex) {
        log.warn("Playlist private or unavailable: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(EmptyPlaylistException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPlaylistException(EmptyPlaylistException ex) {
        log.warn("Empty playlist: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConfigurationException(IllegalStateException ex) {
        log.error("Configuration error: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Server Configuration Error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(YouTubeApiException.class)
    public ResponseEntity<ErrorResponse> handleYouTubeApiException(YouTubeApiException ex) {
        log.error("YouTube API error: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Unable to retrieve playlist videos. Please try again."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
