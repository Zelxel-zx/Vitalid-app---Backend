package com.vitalid.doctor.controller;

import com.vitalid.doctor.entity.Doctor;
import com.vitalid.doctor.repository.DoctorRepository;
import com.vitalid.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Doctor Controller
 * Handles doctor profile and information management
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorController(DoctorRepository doctorRepository, UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<DoctorSummary> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{doctorId}")
    public DoctorSummary getDoctorById(@PathVariable Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(this::toSummary)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    @GetMapping("/specialty/{specialty}")
    public List<DoctorSummary> getDoctorsBySpecialty(@PathVariable String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty).stream()
                .map(this::toSummary)
                .toList();
    }

    @PutMapping("/{doctorId}/status")
    public ResponseEntity<MessageResponse> updateDoctorStatus(@PathVariable Long doctorId, @RequestBody StatusRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        doctor.setStatus(request.status());
        doctorRepository.save(doctor);
        return ResponseEntity.ok(new MessageResponse("Doctor status updated"));
    }

    private DoctorSummary toSummary(Doctor doctor) {
        String doctorName = doctor.getUser() != null ? doctor.getUser().getName() : null;
        return new DoctorSummary(
                doctor.getId(),
                doctorName,
                doctor.getSpecialty(),
                doctor.getAvatar(),
                doctor.getStatus(),
                doctor.getUnreadMessages() == null ? 0 : doctor.getUnreadMessages()
        );
    }

    public record DoctorSummary(Long id, String name, String specialty, String avatar, String status, Integer unreadMessages) {
    }

    public record StatusRequest(String status) {
    }

    public record MessageResponse(String message) {
    }
}

