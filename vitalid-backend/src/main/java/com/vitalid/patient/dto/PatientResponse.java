package com.vitalid.patient.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient Response DTO
 * Used for returning patient data in API responses
 */
@Data
@NoArgsConstructor
public class PatientResponse {

    private Long id;
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String medicalHistory;
    private String allergies;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

