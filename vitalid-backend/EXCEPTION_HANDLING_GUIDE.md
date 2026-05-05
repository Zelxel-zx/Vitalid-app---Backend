# Exception Handling Guide - Vitalid Backend

## Overview

Starting from this version, exception handling has been refactored to use **service-specific exceptions** rather than a centralized exception folder. This provides better code organization, clarity, and maintainability.

## Structure

### Global Exception Folder
The `/exception` folder in the root of `com.vitalid` contains:
- **ErrorResponse.java** - Standard error response model
- **ApiResponse.java** - Standard success response model  
- **HttpStatusCodes.java** - HTTP status code constants
- **GlobalExceptionHandler.java** - Central exception handler for all services
- **BadRequestException.java** - Generic bad request exception (used by all services)
- **ResourceNotFoundException.java** - Generic not found exception (used by all services)
- **UnauthorizedException.java** - Generic unauthorized exception (used by all services)

### Service-Specific Exceptions
Each service module has its own `exception` subfolder:

#### Appointment Service
- `AppointmentNotFoundException` - Thrown when an appointment is not found
- `InvalidAppointmentException` - Thrown when appointment data is invalid

#### Auth Service
- `InvalidCredentialsException` - Thrown when login credentials are invalid
- `TokenExpiredException` - Thrown when a JWT token has expired
- `InvalidTokenException` - Thrown when a JWT token is invalid

#### Chat Service
- `ChatNotFoundException` - Thrown when a chat is not found
- `MessageNotFoundException` - Thrown when a message is not found

#### Checklist Service
- `ChecklistNotFoundException` - Thrown when a checklist is not found
- `InvalidChecklistException` - Thrown when checklist data is invalid

#### Doctor Service
- `DoctorNotFoundException` - Thrown when a doctor is not found
- `InvalidDoctorException` - Thrown when doctor data is invalid

#### Health Service
- `HealthRecordNotFoundException` - Thrown when a health record is not found
- `InvalidHealthDataException` - Thrown when health data is invalid

#### Medication Service
- `MedicationNotFoundException` - Thrown when a medication is not found
- `InvalidMedicationException` - Thrown when medication data is invalid

#### Profile Service
- `ProfileNotFoundException` - Thrown when a profile is not found
- `InvalidProfileException` - Thrown when profile data is invalid

#### Treatment Service
- `TreatmentNotFoundException` - Thrown when a treatment is not found
- `InvalidTreatmentException` - Thrown when treatment data is invalid

## Usage Examples

### Service Layer
```java
package com.vitalid.appointment.service;

import com.vitalid.appointment.exception.AppointmentNotFoundException;
import com.vitalid.appointment.exception.InvalidAppointmentException;

@Service
public class AppointmentService {
    
    public Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
    }
    
    public Appointment createAppointment(AppointmentDTO dto) {
        if (dto.getDate().isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date cannot be in the past");
        }
        // ... create appointment
    }
}
```

### Controller Layer
```java
package com.vitalid.appointment.controller;

import com.vitalid.appointment.service.AppointmentService;
import com.vitalid.appointment.exception.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @GetMapping("/{id}")
    public ApiResponse<AppointmentDTO> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointment(id);
        // Service throws AppointmentNotFoundException if not found
        return ApiResponse.ok(appointmentMapper.toDTO(appointment));
    }
    
    @PostMapping
    public ApiResponse<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO dto) {
        Appointment appointment = appointmentService.createAppointment(dto);
        // Service throws InvalidAppointmentException if data is invalid
        return ApiResponse.ok(appointmentMapper.toDTO(appointment));
    }
}
```

## HTTP Status Code Mapping

| Exception Type | HTTP Status | Description |
|---|---|---|
| `*NotFoundException` | 404 | Resource not found |
| `Invalid*Exception` | 400 | Invalid data/request |
| `InvalidCredentialsException` | 401 | Invalid login credentials |
| `TokenExpiredException` | 401 | JWT token has expired |
| `InvalidTokenException` | 401 | JWT token is invalid |
| `UnauthorizedException` | 401 | User not authorized |
| `BadRequestException` | 400 | Generic bad request |
| Validation errors | 400 | Field validation failed |
| Other exceptions | 500 | Internal server error |

## Global Exception Handler

The `GlobalExceptionHandler` in the global `/exception` folder automatically catches all exceptions and returns standardized `ErrorResponse` objects with appropriate HTTP status codes.

### Error Response Format
```json
{
  "message": "Appointment not found with id: 123",
  "error": "Appointment Not Found",
  "status": 404,
  "path": "/api/appointments/123",
  "timestamp": "2024-05-04T10:30:00",
  "details": null
}
```

## Best Practices

1. **Use service-specific exceptions** in your service layer
2. **Don't catch and suppress exceptions** - let them bubble up to the handler
3. **Provide meaningful error messages** that help users understand what went wrong
4. **Use generic exceptions only** for errors that aren't service-specific
5. **Add detailed validation** before throwing `Invalid*Exception` exceptions
6. **Include request path** in WebRequest for better error tracking

## Migration Guide

If you have existing code using the old global exception imports, update imports like this:

### Before
```java
import com.vitalid.exception.ResourceNotFoundException;
```

### After
```java
// For service-specific exceptions
import com.vitalid.appointment.exception.AppointmentNotFoundException;

// For generic exceptions (still available globally)
import com.vitalid.exception.ResourceNotFoundException;
```

## Adding New Exceptions

If you need to add a new exception for your service:

1. Create the exception file in `your_service/exception/` folder
2. Extend `RuntimeException`
3. Add handler method to `GlobalExceptionHandler` if needed
4. Update this documentation

Example:
```java
package com.vitalid.appointment.exception;

public class AppointmentConflictException extends RuntimeException {
    public AppointmentConflictException(String message) {
        super(message);
    }
    
    public AppointmentConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Then add handler in `GlobalExceptionHandler`:
```java
@ExceptionHandler(AppointmentConflictException.class)
public ResponseEntity<ErrorResponse> handleAppointmentConflictException(
        AppointmentConflictException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        ex.getMessage(),
        "Appointment Conflict",
        HttpStatus.CONFLICT.value(),
        request.getDescription(false).replace("uri=", "")
    );
    logger.warn("Appointment conflict: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
}
```
