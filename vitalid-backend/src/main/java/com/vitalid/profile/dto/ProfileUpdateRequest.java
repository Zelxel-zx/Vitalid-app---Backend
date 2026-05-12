package com.vitalid.profile.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para request de actualizar perfil
 * 
 * TODO: Implement ProfileUpdateRequest with:
 * - name
 * - phone
 * - avatar
 * - bloodType
 * - allergies (list)
 * - specialty (for doctors)
 * - dateOfBirth
 */
@Data
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String name;
    private String phone;
    private String avatar;
    private String bloodType;
    private List<String> allergies;
    private String specialty;
    private LocalDate dateOfBirth;
    private Integer experienceYears;

}

