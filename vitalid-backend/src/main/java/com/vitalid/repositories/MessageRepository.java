package com.vitalid.repositories;

import com.vitalid.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Message Repository
 * Data access for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    List<Message> findBySenderIdOrReceiverIdOrderBySentAtAsc(Long senderId, Long receiverId);
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
            Long senderId,
            Long receiverId,
            Long senderId2,
            Long receiverId2
    );
    List<Message> findBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);
}



