package com.vitalid.dtos.ai;

public record AiConfigResponse(
        boolean groqConfigured,
        String keyPreview,
        int keyLength,
        String model
) {
}
