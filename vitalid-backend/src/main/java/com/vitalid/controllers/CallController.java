package com.vitalid.controllers;

import com.vitalid.models.CallSession;
import com.vitalid.repositories.CallSessionRepository;
import com.vitalid.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired private CallSessionRepository callSessionRepository;
    @Autowired private UserRepository userRepository;

    /** Patient or doctor initiates a call to another user */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateCall(@RequestBody Map<String, Object> body) {
        Long callerUserId = Long.parseLong(body.get("callerUserId").toString());
        Long recipientUserId = Long.parseLong(body.get("recipientUserId").toString());
        String roomName = body.get("roomName").toString();

        var caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller not found"));
        var recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found"));

        // Cancel any existing RINGING calls from this caller to this recipient
        callSessionRepository
                .findByCaller_IdAndStatusOrderByCreatedAtDesc(callerUserId, "RINGING")
                .forEach(c -> {
                    c.setStatus("ENDED");
                    callSessionRepository.save(c);
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
                "status", saved.getStatus()
        ));
    }

    /** Poll for incoming calls (recipient polls this) */
    @GetMapping("/incoming")
    public List<Map<String, Object>> getIncomingCalls(@RequestParam Long recipientUserId) {
        // Auto-expire calls older than 45 seconds
        callSessionRepository.deleteByCreatedAtBeforeAndStatus(
                LocalDateTime.now().minusSeconds(45), "RINGING");

        return callSessionRepository
                .findByRecipient_IdAndStatusOrderByCreatedAtDesc(recipientUserId, "RINGING")
                .stream()
                .map(c -> Map.<String, Object>of(
                        "callId", c.getId(),
                        "callerName", c.getCaller().getName(),
                        "callerUserId", c.getCaller().getId(),
                        "roomName", c.getRoomName(),
                        "status", c.getStatus()
                ))
                .toList();
    }

    /** Accept or reject a call */
    @PutMapping("/{callId}/status")
    public ResponseEntity<Map<String, Object>> updateCallStatus(
            @PathVariable Long callId,
            @RequestBody Map<String, String> body) {
        var session = callSessionRepository.findById(callId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Call not found"));
        session.setStatus(body.get("status")); // ACCEPTED, REJECTED, ENDED
        callSessionRepository.save(session);
        return ResponseEntity.ok(Map.of("status", session.getStatus()));
    }

    /** Cancel/end a call (caller hangs up) */
    @DeleteMapping("/{callId}")
    public ResponseEntity<Void> endCall(@PathVariable Long callId) {
        callSessionRepository.findById(callId).ifPresent(c -> {
            c.setStatus("ENDED");
            callSessionRepository.save(c);
        });
        return ResponseEntity.noContent().build();
    }
}
