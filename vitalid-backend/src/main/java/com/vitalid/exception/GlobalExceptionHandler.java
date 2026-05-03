package com.vitalid.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Global Exception Handler for Vitalid Backend
 * Centralized error handling for all exceptions
 * Returns standardized error responses with appropriate HTTP status codes
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException (404)
     */
    // TODO: Implement exception handler for ResourceNotFoundException
    // - Catch ResourceNotFoundException
    // - Return 404 status with ErrorResponse

    /**
     * Handle BadRequestException (400)
     */
    // TODO: Implement exception handler for BadRequestException
    // - Catch BadRequestException
    // - Return 400 status with ErrorResponse

    /**
     * Handle UnauthorizedException (401)
     */
    // TODO: Implement exception handler for UnauthorizedException
    // - Catch UnauthorizedException
    // - Return 401 status with ErrorResponse

    /**
     * Handle validation errors (400)
     */
    // TODO: Implement exception handler for MethodArgumentNotValidException
    // - Catch validation errors
    // - Build detailed error messages from field errors
    // - Return 400 status with ErrorResponse

    /**
     * Handle general exceptions (500)
     */
    // TODO: Implement exception handler for general Exception
    // - Catch all other exceptions
    // - Log the error
    // - Return 500 status with ErrorResponse

}
