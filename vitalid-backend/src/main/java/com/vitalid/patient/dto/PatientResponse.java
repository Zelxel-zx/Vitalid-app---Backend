package com.vitalid.patient.dto;

import java.time.LocalDate;

public record PatientResponse(
    LocalDate dateOfBirth,
    String bloodType,
    String medicalHistory,
    String allergies
) {}

