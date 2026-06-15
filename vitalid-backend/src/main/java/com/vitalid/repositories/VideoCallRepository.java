package com.vitalid.repositories;

import com.vitalid.models.VideoCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCallRepository extends JpaRepository<VideoCall, Long> {

    List<VideoCall> findByRecipient_IdAndStatusOrderByCreatedAtDesc(
            Long recipientUserId, String status);
}
