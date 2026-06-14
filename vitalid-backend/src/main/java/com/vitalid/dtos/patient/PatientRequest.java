package com.vitalid.dtos.patient;
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
    
    @Pattern(regexp = "^(O|A|B|AB)[+-]?$", message = "Tipo de sangre invÃ¡lido (O, A, B, AB)")
    String bloodType,

    @Size(max = 255, message = "El avatar debe tener como maximo 255 caracteres")
    String avatar,
    
    String address,
    
    String city,
    
    String state,
    
    String zipCode,
    
    String medicalHistory,
    
    String allergies
) {}


