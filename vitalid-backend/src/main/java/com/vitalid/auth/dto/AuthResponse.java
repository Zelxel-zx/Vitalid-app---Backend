package com.vitalid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public record AuthResponse (

    Long id,
    String name,
    String email,   
    UserType type,
){}
