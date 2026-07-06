package com.vitalid.dtos.ai;

import java.util.List;

public record AiAppointmentAction(
        String type,
        Long doctorId,
        String doctorName,
        String specialty,
        String date,
        String time,
        String appointmentType,
        String reason,
        List<String> missing,
        List<String> availableSlots
) {
}
