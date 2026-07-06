package com.vitalid.dtos.ai;

public record RagAskRequest(
        String message,
        AiAppointmentAction pendingAction
) {
}
