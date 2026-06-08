package com.vitalid.controllers;

import com.vitalid.models.Appointment;
import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Controller
 * Handles medical appointments scheduling and management
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public AppointmentController(AppointmentRepository appointmentRepository,
                                 PatientRepository patientRepository,
                                 DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody AppointmentRequest request) {
        var patient = patientRepository.findByUser_Id(request.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found for user ID " + request.patientId()));
        var doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.date());
        appointment.setTime(request.time());
        appointment.setReason(request.reason());
        appointment.setStatus("SCHEDULED");

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public List<AppointmentResponse> listAppointments() {
        return appointmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointment(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    @GetMapping("/doctor/{userId}")
    public List<AppointmentResponse> getAppointmentsForDoctor(@PathVariable Long userId) {
        var doctor = doctorRepository.findByUser_Id(userId);
        if (doctor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found for user ID " + userId);
        }
        return appointmentRepository.findByDoctorId(doctor.getId()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/patient/{userId}")
    public List<AppointmentResponse> getAppointmentsForPatient(@PathVariable Long userId) {
        var patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found for user ID " + userId));
        return appointmentRepository.findByPatientId(patient.getId()).stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (request.date() == null || request.time() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date and time are required");
        }

        LocalDateTime newDateTime = LocalDateTime.of(request.date(), request.time());
        if (newDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reschedule requires at least 2 hours notice");
        }

        appointment.setDate(request.date());
        appointment.setTime(request.time());
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(toResponse(saved));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getUser().getName(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getReason(),
                appointment.getStatus()
        );
    }

    public record AppointmentRequest(Long patientId, Long doctorId, LocalDate date, LocalTime time, String reason) {
    }

    public record RescheduleRequest(LocalDate date, LocalTime time) {
    }

    public record AppointmentResponse(Long id, Long patientId, Long doctorId, String patientName, String doctorName, LocalDate date, LocalTime time, String reason, String status) {
    }
}



