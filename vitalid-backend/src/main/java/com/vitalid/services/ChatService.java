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
        message.setReceiver(doctor.getUser());
        message.setContent(request.getContent().trim());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

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

    private ChatMessageResponse toResponse(Message message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSender(message.getSender() != null ? message.getSender().getName() : null);
        response.setContent(message.getContent());
        response.setTimestamp(message.getSentAt() != null ? message.getSentAt().toString() : null);
        return response;
    }

}



