package com.vitalid.exception;

/**
 * HTTP Status Constants for API Responses
 * Standard HTTP status codes used throughout the application
 */
public class HttpStatusCodes {

    // Success Codes
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;

    // Client Error Codes
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int UNPROCESSABLE_ENTITY = 422;

    // Server Error Codes
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;

    // Custom error message constants
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String INVALID_REQUEST = "Invalid request";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String SERVER_ERROR = "Internal server error";
    public static final String VALIDATION_ERROR = "Validation failed";

}
