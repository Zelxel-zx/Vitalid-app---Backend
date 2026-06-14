package com.vitalid.dtos.doctor;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record DoctorResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String specialty,
        String avatar,
        String medicalCenterAddress,
        String status,
        Integer unreadMessages,
        Boolean verified,
        Integer experienceYears,
        LocalTime availabilityStart,
        LocalTime availabilityEnd,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
