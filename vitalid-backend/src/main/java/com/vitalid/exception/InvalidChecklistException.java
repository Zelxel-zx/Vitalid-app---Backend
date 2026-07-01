package com.vitalid.exception;

/**
 * Exception thrown when checklist data is invalid
 */
public class InvalidChecklistException extends RuntimeException {

    public InvalidChecklistException(String message) {
        super(message);
    }

    public InvalidChecklistException(String message, Throwable cause) {
        super(message, cause);
    }
}



