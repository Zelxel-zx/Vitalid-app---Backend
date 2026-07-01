package com.vitalid.controllers;

import com.vitalid.models.CallSession;
import com.vitalid.repositories.CallSessionRepository;
import com.vitalid.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calls")
public class CallController {

    private final CallSessionRepository callSessionRepository;
    private final UserRepository userRepository;

    public CallController(
            CallSessionRepository callSessionRepository,
            UserRepository userRepository) {
        this.callSessionRepository = callSessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateCall(
            @RequestBody Map<String, Object> body) {
        Long callerUserId = Long.parseLong(body.get("callerUserId").toString());
        Long recipientUserId = Long.parseLong(body.get("recipientUserId").toString());
        String roomName = body.get("roomName").toString();

        var caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Caller not found"));
        var recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipient not found"));

        callSessionRepository
                .findByCaller_IdAndStatusOrderByCreatedAtDesc(callerUserId, "RINGING")
                .forEach(call -> {
                    call.setStatus("ENDED");
                    callSessionRepository.save(call);
                });

        CallSession session = new CallSession();
        session.setCaller(caller);
        session.setRecipient(recipient);
        session.setRoomName(roomName);
        session.setStatus("RINGING");
        CallSession saved = callSessionRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "callId", saved.getId(),
                "roomName", saved.getRoomName(),
                "status", saved.getStatus()));
    }

    @GetMapping("/incoming")
    public List<Map<String, Object>> getIncomingCalls(
            @RequestParam Long recipientUserId) {
        callSessionRepository.deleteByCreatedAtBeforeAndStatus(
                LocalDateTime.now().minusSeconds(45), "RINGING");

        return callSessionRepository
                .findByRecipient_IdAndStatusOrderByCreatedAtDesc(
                        recipientUserId, "RINGING")
                .stream()
                .map(call -> Map.<String, Object>of(
                        "callId", call.getId(),
                        "callerName", call.getCaller().getName(),
                        "callerUserId", call.getCaller().getId(),
                        "roomName", call.getRoomName(),
                        "status", call.getStatus()))
                .toList();
    }

    @PutMapping("/{callId}/status")
    public ResponseEntity<Map<String, Object>> updateCallStatus(
            @PathVariable Long callId,
            @RequestBody Map<String, String> body) {
        var session = callSessionRepository.findById(callId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Call not found"));
        session.setStatus(body.get("status"));
        callSessionRepository.save(session);
        return ResponseEntity.ok(Map.of(
                "callId", session.getId(),
                "status", session.getStatus()));
    }

    @DeleteMapping("/{callId}")
    public ResponseEntity<Void> endCall(@PathVariable Long callId) {
        callSessionRepository.findById(callId).ifPresent(call -> {
            call.setStatus("ENDED");
            callSessionRepository.save(call);
        });
        return ResponseEntity.noContent().build();
    }
}
