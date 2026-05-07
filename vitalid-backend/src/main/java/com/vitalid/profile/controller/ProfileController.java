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
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateProfile(@RequestParam Long userId, @RequestBody ProfileUpdateRequest request) {
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

