package com.example.youtubeextractor.exception;

public class EmptyPlaylistException extends RuntimeException {
    public EmptyPlaylistException(String message) {
        super(message);
    }
}
