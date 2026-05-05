package com.vitalid.checklist.exception;

/**
 * Exception thrown when a checklist is not found
 */
public class ChecklistNotFoundException extends RuntimeException {

    public ChecklistNotFoundException(String message) {
        super(message);
    }

    public ChecklistNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
