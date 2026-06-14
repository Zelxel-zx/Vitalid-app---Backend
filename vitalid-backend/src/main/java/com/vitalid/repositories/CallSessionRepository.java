package com.vitalid.repositories;

import com.vitalid.models.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, Long> {

    List<CallSession> findByRecipient_IdAndStatusOrderByCreatedAtDesc(Long recipientId, String status);

    List<CallSession> findByCaller_IdAndStatusOrderByCreatedAtDesc(Long callerId, String status);

    Optional<CallSession> findTopByCaller_IdAndRecipient_IdAndStatusOrderByCreatedAtDesc(
            Long callerId, Long recipientId, String status);

    void deleteByCreatedAtBeforeAndStatus(LocalDateTime before, String status);
}
