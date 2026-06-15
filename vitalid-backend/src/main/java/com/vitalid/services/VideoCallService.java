package com.vitalid.services;

import com.vitalid.models.VideoCall;
import com.vitalid.repositories.UserRepository;
import com.vitalid.repositories.VideoCallRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VideoCallService {

    private final VideoCallRepository videoCallRepository;
    private final UserRepository userRepository;

    public VideoCallService(VideoCallRepository videoCallRepository,
                            UserRepository userRepository) {
        this.videoCallRepository = videoCallRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VideoCall initiate(Long callerUserId, Long recipientUserId, String roomName) {
        if (callerUserId == null || recipientUserId == null
                || roomName == null || roomName.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Caller, recipient and room are required");
        }

        VideoCall call = new VideoCall();
        call.setCaller(userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Caller not found")));
        call.setRecipient(userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipient not found")));
        call.setRoomName(roomName.trim());
        call.setStatus("RINGING");
        return videoCallRepository.save(call);
    }

    @Transactional
    public List<VideoCall> incoming(Long recipientUserId) {
        List<VideoCall> calls = videoCallRepository
                .findByRecipient_IdAndStatusOrderByCreatedAtDesc(recipientUserId, "RINGING");
        LocalDateTime expiration = LocalDateTime.now().minusSeconds(45);
        calls.stream()
                .filter(call -> call.getCreatedAt().isBefore(expiration))
                .forEach(call -> call.setStatus("MISSED"));
        videoCallRepository.saveAll(calls);
        return calls.stream()
                .filter(call -> "RINGING".equals(call.getStatus()))
                .toList();
    }

    @Transactional
    public VideoCall updateStatus(Long callId, String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("RINGING", "ACCEPTED", "REJECTED", "ENDED", "MISSED")
                .contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid call status");
        }
        VideoCall call = videoCallRepository.findById(callId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Call not found"));
        call.setStatus(normalized);
        return videoCallRepository.save(call);
    }
}
