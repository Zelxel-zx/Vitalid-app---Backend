package com.vitalid.auth.dto;


import com.vitalid.auth.entity.UserType;
import java.time.LocalDateTime;


public record AuthResponse(
    // TODO: Add properties
    Long id,
    String name,
    String email,
    UserType type,
    LocalDateTime createdAt,
    String token,
    String message
) {}

