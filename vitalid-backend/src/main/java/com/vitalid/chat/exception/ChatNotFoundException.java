package com.vitalid.chat.exception;

/**
 * Exception thrown when a chat is not found
 */
public class ChatNotFoundException extends RuntimeException {

    public ChatNotFoundException(String message) {
        super(message);
    }

    public ChatNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

