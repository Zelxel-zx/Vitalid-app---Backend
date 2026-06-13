package com.vitalid.exception;

/**
 * Exception thrown when medication data is invalid
 */
public class InvalidMedicationException extends RuntimeException {

    public InvalidMedicationException(String message) {
        super(message);
    }

    public InvalidMedicationException(String message, Throwable cause) {
        super(message, cause);
    }
}



