package com.vitalid.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Standard API Error Response
 * Used for all error responses from the API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private String details;

    public ErrorResponse(String message, String error, int status) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String error, int status, String path) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
