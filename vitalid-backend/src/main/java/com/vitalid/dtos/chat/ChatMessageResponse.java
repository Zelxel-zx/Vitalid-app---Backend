package com.vitalid.dtos.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de mensaje
 * 
 * TODO: Implement ChatMessageResponse with:
 * - id (message ID)
 * - sender (sender name or ID)
 * - content (message text)
 * - timestamp (when message was sent)
 */
@Data
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String sender;
    private Long senderId;
    private String content;
    private String timestamp;

}



