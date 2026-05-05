package com.vitalid.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Patient Request DTO
 * Used for creating and updating patient data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {

    private Long userId;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String medicalHistory;
    private String allergies;
    private Boolean isActive;
}
