package com.vitalid.dtos.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de enviar mensaje.
 *
 * - doctorId: the Doctor entity ID (always required to locate the conversation thread)
 * - senderId: the user ID of whoever is sending (patient or doctor)
 * - content: message text or base64 data URI for attachments
 * - receiverUserId: when set (doctor sending to patient), this is the patient's user ID;
 *                   when null (patient sending to doctor), the receiver is the doctor's user
 */
@Data
@NoArgsConstructor
public class ChatMessageRequest {
    private Long doctorId;
    private Long senderId;
    private String content;
    /** Optional. Set by doctors when replying to a patient. */
    private Long receiverUserId;
}
