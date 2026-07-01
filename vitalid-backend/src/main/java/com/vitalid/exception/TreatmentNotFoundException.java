package com.vitalid.exception;

/**
 * Exception thrown when a treatment is not found
 */
public class TreatmentNotFoundException extends RuntimeException {

    public TreatmentNotFoundException(String message) {
        super(message);
    }

    public TreatmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}



