package com.vitalid.patient.exception;

/**
 * Exception thrown when patient data is invalid
 */
public class InvalidPatientException extends RuntimeException {

    public InvalidPatientException(String message) {
        super(message);
    }

    public InvalidPatientException(String message, Throwable cause) {
        super(message, cause);
    }
}

