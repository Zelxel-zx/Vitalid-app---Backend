package com.vitalid.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vitalid.dtos.appointment.AppointmentRequest;
import com.vitalid.dtos.appointment.AppointmentResponse;
import com.vitalid.services.AppointmentService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Appointment Controller
 * Handles medical appointment scheduling and queries.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<AppointmentResponse> listAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{appointmentId}")
    public AppointmentResponse getAppointment(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentResponse> getAppointmentsForDoctor(@PathVariable Long doctorId) {
        return appointmentService.getDoctorAppointments(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> getAppointmentsForPatient(@PathVariable Long patientId) {
        return appointmentService.getPatientAppointments(patientId);
    }

    @PutMapping("/{appointmentId}/reschedule")
    public AppointmentResponse rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody RescheduleRequest request) {
        return appointmentService.rescheduleAppointment(
                appointmentId,
                request.date(),
                request.time()
        );
    }

    public record RescheduleRequest(
            LocalDate date,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            @Schema(type = "string", example = "15:30", pattern = "HH:mm")
            LocalTime time) {
    }
}
