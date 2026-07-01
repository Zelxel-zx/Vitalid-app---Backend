package com.vitalid.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vitalid.dtos.profile.ProfileResponse;
import com.vitalid.dtos.profile.ProfileUpdateRequest;
import com.vitalid.dtos.profile.PasswordChangeRequest;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.UserRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.DoctorRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Profile Service
 * Handles user profile management
 * 
 * TODO: Implement methods:
 * - getProfile() -> ProfileResponse
 * - updateProfile(ProfileUpdateRequest) -> ProfileResponse
 * - changePassword(PasswordChangeRequest) -> void
 * - uploadAvatar(file) -> String (avatar URL)
 * - deleteProfile() -> void
 */
@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toProfileResponse(user);
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        if (user.getType() == UserType.PATIENT) {
            var patient = patientRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));
            if (request.getBloodType() != null) {
                patient.setBloodType(request.getBloodType());
            }
            if (request.getAllergies() != null) {
                patient.setAllergies(String.join(",", request.getAllergies()));
            }
            if (request.getDateOfBirth() != null) {
                patient.setDateOfBirth(request.getDateOfBirth());
            }
            if (request.getAvatar() != null) {
                patient.setAvatar(normalize(request.getAvatar()));
            }
            patientRepository.save(patient);
        }

        if (user.getType() == UserType.DOCTOR) {
            var doctor = doctorRepository.findByUser_Id(userId);
            if (doctor == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found");
            }
            if (request.getSpecialty() != null) {
                doctor.setSpecialty(request.getSpecialty());
            }
            if (request.getAvatar() != null) {
                doctor.setAvatar(request.getAvatar());
            }
            if (request.getExperienceYears() != null) {
                doctor.setExperienceYears(request.getExperienceYears());
            }
            doctorRepository.save(doctor);
        }

        return toProfileResponse(user);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old and new passwords are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setType(user.getType().name());

        if (user.getType() == UserType.PATIENT) {
            var patient = patientRepository.findByUser_Id(user.getId()).orElse(null);
            if (patient != null) {
                response.setBloodType(patient.getBloodType());
                response.setDateOfBirth(patient.getDateOfBirth());
                response.setAllergies(splitCsv(patient.getAllergies()));
                response.setAvatar(patient.getAvatar());
            }
        }

        if (user.getType() == UserType.DOCTOR) {
            var doctor = doctorRepository.findByUser_Id(user.getId());
            if (doctor != null) {
                response.setAvatar(doctor.getAvatar());
                response.setSpecialty(doctor.getSpecialty());
                response.setExperienceYears(doctor.getExperienceYears());
                response.setVerified(doctor.getVerified());
            }
        }

        return response;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

}



