package com.vitalid.dtos.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageRequest {
    private Long doctorId;
    private Long senderId;
    private Long receiverUserId;
    private String content;
}