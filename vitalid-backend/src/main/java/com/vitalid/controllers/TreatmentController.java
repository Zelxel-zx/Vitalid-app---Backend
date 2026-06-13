package com.vitalid.controllers;

import com.vitalid.dtos.medication.MedicationRequest;
import com.vitalid.dtos.medication.MedicationResponse;
import com.vitalid.dtos.treatment.TreatmentRequest;
import com.vitalid.dtos.treatment.TreatmentResponse;
import com.vitalid.services.MedicationService;
import com.vitalid.services.TreatmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Treatment Controller
 * Handles treatment plans prescribed by doctors to patients.
 */
@RestController
@RequestMapping("/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final MedicationService medicationService;

    public TreatmentController(TreatmentService treatmentService,
                               MedicationService medicationService) {
        this.treatmentService = treatmentService;
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<TreatmentResponse> listTreatments() {
        return treatmentService.getAllTreatments();
    }

    @GetMapping("/{treatmentId}")
    public TreatmentResponse getTreatmentById(@PathVariable Long treatmentId) {
        return treatmentService.getTreatmentById(treatmentId);
    }

    @GetMapping("/active")
    public List<TreatmentResponse> getActiveTreatments() {
        return treatmentService.getTreatmentsByStatus("ACTIVE");
    }

    @GetMapping("/completed")
    public List<TreatmentResponse> getCompletedTreatments() {
        return treatmentService.getTreatmentsByStatus("COMPLETED");
    }

    @GetMapping("/patient/{patientId}")
    public List<TreatmentResponse> getTreatmentsByPatient(@PathVariable Long patientId) {
        return treatmentService.getTreatmentsByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<TreatmentResponse> getTreatmentsByDoctor(@PathVariable Long doctorId) {
        return treatmentService.getTreatmentsByDoctorId(doctorId);
    }

    @PostMapping
    public ResponseEntity<TreatmentResponse> createTreatment(@RequestBody TreatmentRequest request) {
        TreatmentResponse response = treatmentService.createTreatment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{treatmentId}")
    public TreatmentResponse updateTreatment(
            @PathVariable Long treatmentId,
            @RequestBody TreatmentRequest request) {
        return treatmentService.updateTreatment(treatmentId, request);
    }

    @PostMapping("/{treatmentId}/medications")
    public ResponseEntity<MedicationResponse> createMedication(
            @PathVariable Long treatmentId,
            @RequestBody MedicationRequest request) {
        MedicationResponse response = medicationService.createMedication(treatmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{treatmentId}")
    public ResponseEntity<Void> deleteTreatment(@PathVariable Long treatmentId) {
        treatmentService.deleteTreatment(treatmentId);
        return ResponseEntity.noContent().build();
    }
}
