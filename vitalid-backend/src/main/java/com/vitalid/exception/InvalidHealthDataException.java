package com.vitalid.exception;

/**
 * Exception thrown when health data is invalid
 */
public class InvalidHealthDataException extends RuntimeException {

    public InvalidHealthDataException(String message) {
        super(message);
    }

    public InvalidHealthDataException(String message, Throwable cause) {
        super(message, cause);
    }
}



