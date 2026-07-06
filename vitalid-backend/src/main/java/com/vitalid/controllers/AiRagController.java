package com.vitalid.controllers;

import com.vitalid.dtos.ai.AiConfirmAppointmentRequest;
import com.vitalid.dtos.ai.RagAskRequest;
import com.vitalid.dtos.ai.RagAskResponse;
import com.vitalid.dtos.ai.RagIngestResponse;
import com.vitalid.dtos.appointment.AppointmentResponse;
import com.vitalid.services.AiRagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/chat")
public class AiRagController {

    private final AiRagService aiRagService;

    public AiRagController(AiRagService aiRagService) {
        this.aiRagService = aiRagService;
    }

    @PostMapping("/ingest")
    public RagIngestResponse ingest() {
        return aiRagService.ingestMedicalKnowledge();
    }

    @PostMapping("/ask")
    public RagAskResponse ask(@RequestBody RagAskRequest request) {
        return aiRagService.ask(request);
    }

    @PostMapping("/confirm")
    public AppointmentResponse confirm(@RequestBody AiConfirmAppointmentRequest request) {
        return aiRagService.confirmAppointment(request.action());
    }
}
