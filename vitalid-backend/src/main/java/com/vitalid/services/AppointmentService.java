package com.vitalid.services;

import com.vitalid.dtos.appointment.AppointmentRequest;
import com.vitalid.dtos.appointment.AppointmentResponse;
import com.vitalid.models.Appointment;
import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByDateAscTimeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponse getAppointmentById(Long appointmentId) {
        return toResponse(findAppointment(appointmentId));
    }

    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        ensureDoctorExists(doctorId);
        return appointmentRepository.findByDoctorIdOrderByDateAscTimeAsc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        ensurePatientExists(patientId);
        return appointmentRepository.findByPatientIdOrderByDateAscTimeAsc(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Patient patient = ensurePatientExists(requiredId(request.getPatientId(), "Patient id is required"));
        Doctor doctor = ensureDoctorExists(requiredId(request.getDoctorId(), "Doctor id is required"));
        validateDateAndTime(request.getDate(), request.getTime());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(Optional.ofNullable(request.getStatus()).orElse("SCHEDULED"));

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, LocalDate date, LocalTime time) {
        Appointment appointment = findAppointment(appointmentId);
        validateDateAndTime(date, time);

        LocalDateTime newDateTime = LocalDateTime.of(date, time);
        if (newDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Reschedule requires at least 2 hours notice");
        }

        appointment.setDate(date);
        appointment.setTime(time);
        return toResponse(appointmentRepository.save(appointment));
    }

    private Appointment findAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private Patient ensurePatientExists(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    private Doctor ensureDoctorExists(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    private Long requiredId(Long value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private void validateDateAndTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date and time are required");
        }
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getId(),
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getUser().getName(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getReason(),
                appointment.getStatus()
        );
    }
}
