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
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
    }

    @GetMapping("/unread")
    public List<UnreadResponse> getUnreadMessages() {
    }

    @PutMapping("/read/{doctorId}")
    public ResponseEntity<MessageResponse> markRead(@PathVariable Long doctorId) {
    }

    private ChatMessageResponse toResponse(Message message) {
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

