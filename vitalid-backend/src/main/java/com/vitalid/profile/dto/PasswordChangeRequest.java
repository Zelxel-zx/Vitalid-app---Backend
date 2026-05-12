package com.vitalid.profile.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de cambiar contraseña
 * 
 * TODO: Implement PasswordChangeRequest with:
 * - oldPassword
 * - newPassword
 */
@Data
@NoArgsConstructor
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;

}

