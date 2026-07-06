package com.vitalid.dtos.patient;

public record PatientNoteRequest(
        Long patientId,
        String title,
        String content
) {
}
