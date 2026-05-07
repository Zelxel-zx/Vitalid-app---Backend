package com.vitalid.auth.dto;


import com.vitalid.auth.entity.UserType;


public record AuthResponse(
    // TODO: Add properties
    Long id,
    String name,
    String email,
    UserType type,
    String token,
    String message
) {}

