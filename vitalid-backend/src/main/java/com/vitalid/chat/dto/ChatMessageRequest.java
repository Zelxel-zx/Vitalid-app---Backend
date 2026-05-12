package com.vitalid.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de enviar mensaje
 * 
 * TODO: Implement ChatMessageRequest with:
 * - doctorId (receiver)
 * - content (message text)
 * - senderId (sender)
 */
@Data
@NoArgsConstructor
public class ChatMessageRequest {
    private Long doctorId;
    private Long senderId;
    private String content;

}

