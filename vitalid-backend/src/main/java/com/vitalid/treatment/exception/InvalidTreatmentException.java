package com.vitalid.treatment.exception;

/**
 * Exception thrown when treatment data is invalid
 */
public class InvalidTreatmentException extends RuntimeException {

    public InvalidTreatmentException(String message) {
        super(message);
    }

    public InvalidTreatmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
