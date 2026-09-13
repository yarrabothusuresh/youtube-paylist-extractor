package com.example.youtubeextractor.exception;

public class InvalidPlaylistException extends RuntimeException {
    public InvalidPlaylistException(String message) {
        super(message);
    }
}
