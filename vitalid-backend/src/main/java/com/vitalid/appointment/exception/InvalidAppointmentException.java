package com.vitalid.appointment.exception;

/**
 * Exception thrown when appointment data is invalid
 */
public class InvalidAppointmentException extends RuntimeException {

    public InvalidAppointmentException(String message) {
        super(message);
    }

    public InvalidAppointmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
