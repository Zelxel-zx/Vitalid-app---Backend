package com.vitalid.doctor.exception;

/**
 * Exception thrown when doctor data is invalid
 */
public class InvalidDoctorException extends RuntimeException {

    public InvalidDoctorException(String message) {
        super(message);
    }

    public InvalidDoctorException(String message, Throwable cause) {
        super(message, cause);
    }
}
