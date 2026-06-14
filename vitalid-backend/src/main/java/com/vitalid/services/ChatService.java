package com.vitalid.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.vitalid.dtos.chat.ChatMessageRequest;
import com.vitalid.dtos.chat.ChatMessageResponse;
import com.vitalid.dtos.chat.UnreadResponse;
import com.vitalid.models.Message;
import com.vitalid.repositories.MessageRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Chat Service
 * Handles messaging operations
 * 
 * TODO: Implement methods:
 * - getMessagesByDoctorId(Integer doctorId) -> List<ChatMessageResponse>
 * - sendMessage(ChatMessageRequest) -> ChatMessageResponse
 * - getUnreadMessages() -> List<UnreadResponse>
 * - markMessagesAsRead(Integer doctorId) -> void
 * - getConversations() -> List<ConversationDTO>
 * - deleteMessage(String messageId) -> void
 */
@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ChatMessageResponse> getMessagesByDoctorId(Long doctorId, Long userId) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Long doctorUserId = doctor.getUser().getId();

        List<Message> messages;
        if (userId == null) {
            messages = messageRepository.findBySenderIdOrReceiverIdOrderBySentAtAsc(doctorUserId, doctorUserId);
        } else {
            messages = messageRepository
                    .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                            doctorUserId,
                            userId,
                            userId,
                            doctorUserId
                    );
        }

        return messages.stream().map(this::toResponse).toList();
    }

    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        if (request.getDoctorId() == null || request.getSenderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor and sender are required");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }

        var doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        var sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setContent(request.getContent().trim());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        // If receiverUserId is provided, the doctor is sending to a specific patient.
        // Otherwise (patient sending), the receiver is the doctor's user account.
        if (request.getReceiverUserId() != null) {
            var receiver = userRepository.findById(request.getReceiverUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));
            message.setReceiver(receiver);
        } else {
            message.setReceiver(doctor.getUser());
        }

        Message saved = messageRepository.save(message);
        return toResponse(saved);
    }

    public List<UnreadResponse> getUnreadMessages(Long receiverId) {
        if (receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver is required");
        }

        List<Message> unread = messageRepository.findByReceiverIdAndIsReadFalse(receiverId);
        Map<Long, Long> unreadBySender = unread.stream()
                .collect(Collectors.groupingBy(m -> m.getSender().getId(), Collectors.counting()));

        return unreadBySender.entrySet().stream().map(entry -> {
            var doctor = doctorRepository.findByUser_Id(entry.getKey());
            Long doctorId = doctor != null ? doctor.getId() : null;
            UnreadResponse response = new UnreadResponse();
            response.setDoctorId(doctorId);
            response.setUnreadCount(entry.getValue());
            return response;
        }).toList();
    }

    public void markMessagesAsRead(Long doctorId, Long receiverId) {
        if (doctorId == null || receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor and receiver are required");
        }
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Long doctorUserId = doctor.getUser().getId();

        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdAndIsReadFalse(
                doctorUserId,
                receiverId
        );
        for (Message message : messages) {
            message.setIsRead(true);
        }
        messageRepository.saveAll(messages);
    }

    /** Returns the total number of unread messages for a given user — used by the nav badge. */
    public long getTotalUnreadCount(Long receiverId) {
        if (receiverId == null) return 0;
        return messageRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    /**
     * Returns the list of distinct patients who have exchanged messages with a doctor.
     * Used to populate the doctor's conversation inbox.
     */
    public List<ConversationSummary> getConversationsForDoctor(Long doctorId) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Long doctorUserId = doctor.getUser().getId();

        // All messages involving the doctor
        List<Message> allMessages = messageRepository.findBySenderIdOrReceiverIdOrderBySentAtAsc(doctorUserId, doctorUserId);

        // Collect distinct other-party user IDs
        return allMessages.stream()
                .map(m -> {
                    Long otherUserId = m.getSender().getId().equals(doctorUserId)
                            ? (m.getReceiver() != null ? m.getReceiver().getId() : null)
                            : m.getSender().getId();
                    String otherName = m.getSender().getId().equals(doctorUserId)
                            ? (m.getReceiver() != null ? m.getReceiver().getName() : null)
                            : m.getSender().getName();
                    String preview = m.getContent();
                    if (preview != null && preview.startsWith("data:")) {
                        preview = preview.startsWith("data:image") ? "[Imagen]" : "[PDF]";
                    }
                    return new ConversationSummary(otherUserId, otherName, preview, m.getSentAt() != null ? m.getSentAt().toString() : null);
                })
                .filter(c -> c.patientUserId() != null && !c.patientUserId().equals(doctorUserId))
                .collect(java.util.stream.Collectors.toMap(
                        ConversationSummary::patientUserId,
                        c -> c,
                        (existing, replacement) -> replacement // keep latest message
                ))
                .values().stream()
                .toList();
    }

    public record ConversationSummary(Long patientUserId, String patientName, String lastMessage, String lastMessageAt) {}

    private ChatMessageResponse toResponse(Message message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSender(message.getSender() != null ? message.getSender().getName() : null);
        response.setContent(message.getContent());
        response.setTimestamp(message.getSentAt() != null ? message.getSentAt().toString() : null);
        return response;
    }

}



