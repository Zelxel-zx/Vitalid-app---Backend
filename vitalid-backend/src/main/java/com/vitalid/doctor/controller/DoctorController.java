package com.vitalid.doctor.controller;

import com.vitalid.doctor.entity.Doctor;
import com.vitalid.doctor.repository.DoctorRepository;
import com.vitalid.appointment.repository.AppointmentRepository;
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
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public DoctorController(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
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
                .map(appointment -> appointment.getTime())
                .collect(Collectors.toSet());

        List<String> available = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            if (!bookedTimes.contains(current)) {
                available.add(current.toString());
            }
            current = current.plusMinutes(30);
        }

        return new AvailabilityResponse(available, start.toString(), end.toString());
    }

    private DoctorSummary toSummary(Doctor doctor) {
        String doctorName = doctor.getUser() != null ? doctor.getUser().getName() : null;
        return new DoctorSummary(
                doctor.getId(),
                doctorName,
                doctor.getSpecialty(),
                doctor.getAvatar(),
                doctor.getStatus(),
                doctor.getUnreadMessages() == null ? 0 : doctor.getUnreadMessages(),
                doctor.getExperienceYears(),
                doctor.getVerified() == null ? Boolean.FALSE : doctor.getVerified()
        );
    }

    public record DoctorSummary(Long id, String name, String specialty, String avatar, String status, Integer unreadMessages, Integer experienceYears, Boolean verified) {
    }

    public record StatusRequest(String status) {
    }

    public record VerifyRequest(Boolean verified) {
    }

    public record AvailabilityRequest(String startTime, String endTime) {
    }

    public record AvailabilityResponse(List<String> availableSlots, String startTime, String endTime) {
    }

    public record MessageResponse(String message) {
    }
}

