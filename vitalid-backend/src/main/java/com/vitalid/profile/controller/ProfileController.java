package com.vitalid.profile.controller;

import com.vitalid.auth.entity.User;
import com.vitalid.auth.repository.UserRepository;
import com.vitalid.patient.entity.Patient;
import com.vitalid.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Profile Controller
 * Handles user profile information and settings
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public ProfileController(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public ProfileResponse getProfile(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Optional<Patient> patient = patientRepository.findByUserId(user.getId());

        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getType(),
                patient.map(Patient::getBloodType).orElse(null),
                patient.map(Patient::getAddress).orElse(null),
                patient.map(Patient::getCity).orElse(null),
                patient.map(Patient::getState).orElse(null),
                patient.map(Patient::getZipCode).orElse(null),
                patient.map(Patient::getMedicalHistory).orElse(null),
                patient.map(Patient::getAllergies).orElse(null)
        );
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateProfile(@RequestParam Long userId, @RequestBody ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
    }

    public record ProfileUpdateRequest(String name, String phone) {
    }

    public record ProfileResponse(Long id, String name, String email, String phone, String type,
                                  String bloodType, String address, String city, String state, String zipCode,
                                  String medicalHistory, String allergies) {
    }

    public record MessageResponse(String message) {
    }
}

