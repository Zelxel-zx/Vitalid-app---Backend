package com.vitalid.dtos.profile;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para response de perfil
 * 
 * TODO: Implement ProfileResponse with:
 * - id
 * - name
 * - email
 * - phone
 * - type (PATIENT, DOCTOR)
 * - avatar
 * - bloodType
 * - allergies (list)
 * - dateOfBirth
 * - specialty (for doctors)
 */
@Data
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String type;
    private String avatar;
    private String bloodType;
    private List<String> allergies;
    private LocalDate dateOfBirth;
    private String specialty;
    private Integer experienceYears;
    private Boolean verified;

}



