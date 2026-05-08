package com.vitalid.medication.exception;

/**
 * Exception thrown when a medication is not found
 */
public class MedicationNotFoundException extends RuntimeException {

    public MedicationNotFoundException(String message) {
        super(message);
    }

    public MedicationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

