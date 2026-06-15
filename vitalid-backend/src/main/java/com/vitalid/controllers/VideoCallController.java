package com.vitalid.controllers;

import com.vitalid.models.VideoCall;
import com.vitalid.services.VideoCallService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calls")
public class VideoCallController {

    private final VideoCallService videoCallService;

    public VideoCallController(VideoCallService videoCallService) {
        this.videoCallService = videoCallService;
    }

    @PostMapping("/initiate")
    public InitiateResponse initiate(@RequestBody InitiateRequest request) {
        VideoCall call = videoCallService.initiate(
                request.callerUserId(), request.recipientUserId(), request.roomName());
        return new InitiateResponse(call.getId(), call.getRoomName());
    }

    @GetMapping("/incoming")
    public List<IncomingResponse> incoming(@RequestParam Long recipientUserId) {
        return videoCallService.incoming(recipientUserId).stream()
                .map(call -> new IncomingResponse(
                        call.getId(),
                        call.getCaller().getName(),
                        call.getCaller().getId(),
                        call.getRoomName(),
                        call.getStatus()))
                .toList();
    }

    @PutMapping("/{callId}/status")
    public StatusResponse updateStatus(
            @PathVariable Long callId,
            @RequestBody StatusRequest request) {
        VideoCall call = videoCallService.updateStatus(callId, request.status());
        return new StatusResponse(call.getId(), call.getStatus());
    }

    public record InitiateRequest(Long callerUserId, Long recipientUserId, String roomName) {
    }

    public record InitiateResponse(Long callId, String roomName) {
    }

    public record IncomingResponse(
            Long callId,
            String callerName,
            Long callerUserId,
            String roomName,
            String status) {
    }

    public record StatusRequest(String status) {
    }

    public record StatusResponse(Long callId, String status) {
    }
}
