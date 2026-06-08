package com.vitalid.dtos.auth;


import com.vitalid.models.UserType;
import java.time.LocalDateTime;


public record AuthResponse(
    Long id,
    String name,
    String email,
    UserType type,
    LocalDateTime createdAt,
    String token,
    String message
) {}




