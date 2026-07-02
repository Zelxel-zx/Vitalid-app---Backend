package com.vitalid.dtos.ai;

public record RagIngestResponse(
        String message,
        int chunks
) {
}
