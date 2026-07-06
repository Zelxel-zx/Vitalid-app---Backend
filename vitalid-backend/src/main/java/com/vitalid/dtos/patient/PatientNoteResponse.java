package com.vitalid.dtos.patient;

import java.time.LocalDateTime;

public record PatientNoteResponse(
        Long id,
        Long doctorId,
        Long patientId,
        String patientName,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
