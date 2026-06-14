package com.vitalid.controllers;

import com.vitalid.dtos.doctor.DoctorRequest;
import com.vitalid.dtos.doctor.DoctorResponse;
import com.vitalid.exception.ApiResponse;
import com.vitalid.models.Doctor;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Doctor Controller
 * Handles doctor profile and information management
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorRepository doctorRepository,
                            AppointmentRepository appointmentRepository,
                            DoctorService doctorService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctorProfile(
            @Valid @RequestBody DoctorRequest request) {
        DoctorResponse doctor = doctorService.createOrCompleteProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Doctor profile completed successfully", doctor));
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

    @PutMapping("/{doctorId}/verify")
    public ResponseEntity<MessageResponse> verifyDoctor(@PathVariable Long doctorId, @RequestBody VerifyRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        boolean verified = request != null && request.verified() != null ? request.verified() : true;
        doctor.setVerified(verified);
        doctorRepository.save(doctor);
        return ResponseEntity.ok(new MessageResponse("Doctor verification updated"));
    }

    @PutMapping("/{doctorId}/availability")
    public ResponseEntity<MessageResponse> updateAvailability(@PathVariable Long doctorId, @RequestBody AvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (request == null || request.startTime() == null || request.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end time are required");
        }

        LocalTime start = LocalTime.parse(request.startTime());
        LocalTime end = LocalTime.parse(request.endTime());
        if (end.isBefore(start) || end.equals(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        doctor.setAvailabilityStart(start);
        doctor.setAvailabilityEnd(end);
        doctorRepository.save(doctor);
        return ResponseEntity.ok(new MessageResponse("Availability updated"));
    }

    @GetMapping("/{doctorId}/availability")
    public AvailabilityResponse getAvailability(
            @PathVariable Long doctorId,
            @RequestParam("date") LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        LocalTime start = doctor.getAvailabilityStart() != null ? doctor.getAvailabilityStart() : LocalTime.of(9, 0);
        LocalTime end = doctor.getAvailabilityEnd() != null ? doctor.getAvailabilityEnd() : LocalTime.of(17, 0);

        Set<LocalTime> bookedTimes = appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .filter(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()))
                .map(appointment -> appointment.getTime())
                .collect(Collectors.toSet());

        List<String> available = new ArrayList<>();
        List<String> occupied = new ArrayList<>();
        if (date.isBefore(LocalDate.now())) {
            return new AvailabilityResponse(available, occupied, start.toString(), end.toString());
        }
        LocalTime current = start;
        while (current.isBefore(end)) {
            boolean futureSlot = !date.equals(LocalDate.now()) || current.isAfter(LocalTime.now());
            if (futureSlot) {
                if (bookedTimes.contains(current)) {
                    occupied.add(current.toString());
                } else {
                    available.add(current.toString());
                }
            }
            current = current.plusMinutes(30);
        }

        return new AvailabilityResponse(available, occupied, start.toString(), end.toString());
    }

    private DoctorSummary toSummary(Doctor doctor) {
        String doctorName = doctor.getUser() != null ? doctor.getUser().getName() : null;
        Long userId = doctor.getUser() != null ? doctor.getUser().getId() : null;
        return new DoctorSummary(
                doctor.getId(),
                userId,
                doctorName,
                doctor.getSpecialty(),
                doctor.getAvatar(),
                doctor.getMedicalCenterAddress(),
                doctor.getStatus(),
                doctor.getUnreadMessages() == null ? 0 : doctor.getUnreadMessages(),
                doctor.getExperienceYears(),
                doctor.getVerified() == null ? Boolean.FALSE : doctor.getVerified()
        );
    }

    public record DoctorSummary(Long id, Long userId, String name, String specialty, String avatar, String medicalCenterAddress, String status, Integer unreadMessages, Integer experienceYears, Boolean verified) {
    }

    public record StatusRequest(String status) {
    }

    public record VerifyRequest(Boolean verified) {
    }

    public record AvailabilityRequest(String startTime, String endTime) {
    }

    public record AvailabilityResponse(
            List<String> availableSlots,
            List<String> occupiedSlots,
            String startTime,
            String endTime) {
    }

    public record MessageResponse(String message) {
    }
}



