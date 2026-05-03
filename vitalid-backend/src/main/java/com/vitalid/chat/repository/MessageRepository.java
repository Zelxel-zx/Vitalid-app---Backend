package com.vitalid.chat.repository;

import com.vitalid.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Message Repository
 * Data access for Message entity
 * 
 * TODO: Implement query methods:
 * - findBySenderIdAndReceiverId(Integer senderId, Integer receiverId)
 * - findUnreadMessagesByReceiverId(Integer receiverId)
 * - findByIsReadFalseAndReceiverId(Integer receiverId)
 * - countUnreadByReceiverId(Integer receiverId)
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    // TODO: Add custom query methods

}
