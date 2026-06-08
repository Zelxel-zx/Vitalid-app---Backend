package com.vitalid.controllers;

import com.vitalid.models.Treatment;
import com.vitalid.repositories.TreatmentRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Treatment Controller
 * Handles treatment plans and medical treatment management
 */
@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public TreatmentController(TreatmentRepository treatmentRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.treatmentRepository = treatmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<TreatmentResponse> listTreatments() {
        return treatmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/active")
    public List<TreatmentResponse> getActiveTreatments() {
        return treatmentRepository.findByStatus("ACTIVE").stream().map(this::toResponse).toList();
    }

    @GetMapping("/completed")
    public List<TreatmentResponse> getCompletedTreatments() {
        return treatmentRepository.findByStatus("COMPLETED").stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<TreatmentResponse> createTreatment(@RequestBody TreatmentRequest request) {
        var patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        var doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setDoctor(doctor);
        treatment.setTitle(request.title());
        treatment.setStatus(request.status() == null ? "ACTIVE" : request.status());
        treatment.setProgress(request.progress() == null ? 0 : request.progress());
        treatment.setNextAppointment(request.nextAppointment());
        treatment.setMedications(request.medications() == null ? null : String.join(",", request.medications()));

        Treatment saved = treatmentRepository.save(treatment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{treatmentId}/progress")
    public ResponseEntity<MessageResponse> updateProgress(@PathVariable Long treatmentId, @RequestBody ProgressRequest request) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment not found"));
        if (request.progress() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Progress value required");
        }
        treatment.setProgress(request.progress());
        treatmentRepository.save(treatment);
        return ResponseEntity.ok(new MessageResponse("Treatment progress updated"));
    }

    private TreatmentResponse toResponse(Treatment treatment) {
        List<String> medications = treatment.getMedications() == null ? List.of()
                : List.of(treatment.getMedications().split(","));
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getTitle(),
                treatment.getDoctor().getUser().getName(),
                treatment.getStatus(),
                treatment.getProgress(),
                treatment.getNextAppointment(),
                medications
        );
    }

    public record TreatmentRequest(Long patientId, Long doctorId, String title, String status, Integer progress, List<String> medications, LocalDate nextAppointment) {
    }

    public record ProgressRequest(Integer progress) {
    }

    public record TreatmentResponse(Long id, String title, String doctor, String status, Integer progress, LocalDate nextAppointment, List<String> medications) {
    }

    public record MessageResponse(String message) {
    }
}



