package com.vitalid.dtos.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de mensajes sin leer
 * 
 * TODO: Implement UnreadResponse with:
 * - doctorId (doctor ID)
 * - unreadCount (number of unread messages)
 */
@Data
@NoArgsConstructor
public class UnreadResponse {
    private Long doctorId;
    private Long senderUserId;  // userId of the sender (for patient→doctor messages)
    private long unreadCount;
}




