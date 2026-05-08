package com.vitalid.health.exception;

/**
 * Exception thrown when a health record is not found
 */
public class HealthRecordNotFoundException extends RuntimeException {

    public HealthRecordNotFoundException(String message) {
        super(message);
    }

    public HealthRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

