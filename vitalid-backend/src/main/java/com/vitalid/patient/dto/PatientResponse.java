package com.vitalid.patient.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
    Long id,
    LocalDate dateOfBirth,
    String bloodType,
    String address,
    String city,
    String state,
    String zipCode,
    String medicalHistory,
    String allergies,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long userId,
    String name,
    String email
) {}
