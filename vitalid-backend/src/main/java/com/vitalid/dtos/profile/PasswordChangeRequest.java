package com.vitalid.dtos.profile;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de cambiar contraseÃ±a
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



