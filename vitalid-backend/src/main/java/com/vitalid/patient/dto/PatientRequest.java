package com.vitalid.patient.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Patient Request DTO
 * Used for creating and updating patient-specific data
 * (User fields like email, password, phone are inherited from User)
 */
public record PatientRequest(
    
    @NotNull(message = "Fecha de nacimiento es obligatoria")
    LocalDate dateOfBirth,
    
    @Pattern(regexp = "^(O|A|B|AB)[+-]?$", message = "Tipo de sangre inválido (O, A, B, AB)")
    String bloodType,
    
    String address,
    
    String city,
    
    String state,
    
    String zipCode,
    
    String medicalHistory,
    
    String allergies
) {}
