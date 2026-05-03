package com.vitalid.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Message Entity
 * Represents a chat message between users
 * 
 * TODO: Implement message entity with:
 * - id (auto-generated)
 * - senderId (foreign key)
 * - receiverId (foreign key)
 * - content (message text)
 * - timestamp
 * - isRead (boolean)
 * - createdAt
 * - relationships to User entities
 */
@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    // TODO: Add entity properties and annotations

}
