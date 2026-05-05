package com.vitalid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de autenticación
 * 
 * TODO: Implement AuthResponse with:
 * - id (user ID)
 * - name
 * - email
 * - type (PATIENT, DOCTOR)
 * - token (JWT)
 * - message
 */
public record AuthResponse (

    // TODO: Add properties
    Long id,
    String name,
    String email,
    UserType type,
    String token,
    String message
) {}
