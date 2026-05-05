package com.vitalid.chat.controller;

import com.vitalid.chat.entity.Message;
import com.vitalid.chat.repository.MessageRepository;
import com.vitalid.doctor.entity.Doctor;
import com.vitalid.doctor.repository.DoctorRepository;
import com.vitalid.auth.entity.User;
import com.vitalid.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat Controller
 * Handles messaging between patients and doctors
 */
@RestController
@RequestMapping({"/api/chat", "/api/messages"})
public class ChatController {

    private final MessageRepository messageRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatController(MessageRepository messageRepository,
                          DoctorRepository doctorRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/doctor/{doctorId}")
    public List<ChatMessageResponse> getMessagesForDoctor(@PathVariable Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Long doctorUserId = doctor.getUser().getId();
        return messageRepository.findBySenderIdOrReceiverIdOrderBySentAtAsc(doctorUserId, doctorUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(doctor.getUser());
        message.setContent(request.content());
        message.setIsRead(false);
        message.setSentAt(LocalDateTime.now());

        ChatMessageResponse response = toResponse(messageRepository.save(message));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/unread")
    public List<UnreadResponse> getUnreadMessages() {
        return doctorRepository.findAll().stream()
                .map(doctor -> new UnreadResponse(
                        doctor.getId(),
                        messageRepository.countByReceiverIdAndIsReadFalse(doctor.getUser().getId())
                ))
                .toList();
    }

    @PutMapping("/read/{doctorId}")
    public ResponseEntity<MessageResponse> markRead(@PathVariable Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Long doctorUserId = doctor.getUser().getId();
        messageRepository.findByReceiverIdAndIsReadFalse(doctorUserId).forEach(message -> {
            message.setIsRead(true);
            messageRepository.save(message);
        });
        return ResponseEntity.ok(new MessageResponse("Messages marked as read"));
    }

    private ChatMessageResponse toResponse(Message message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getSentAt().toString()
        );
    }

    public record SendMessageRequest(Long doctorId, Long senderId, String content) {
    }

    public record ChatMessageResponse(Long id, String sender, String content, String timestamp) {
    }

    public record UnreadResponse(Long doctorId, long unreadCount) {
    }

    public record MessageResponse(String message) {
    }
}

