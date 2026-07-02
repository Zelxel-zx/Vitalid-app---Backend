package com.vitalid.dtos.ai;

import java.util.List;

public record RagAskResponse(
        String reply,
        boolean answeredFromDocs,
        List<String> sources
) {
}
