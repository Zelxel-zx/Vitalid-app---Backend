package com.vitalid.controllers;

import com.vitalid.dtos.medication.MedicationRequest;
import com.vitalid.dtos.medication.MedicationResponse;
import com.vitalid.services.MedicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Medication Controller
 * Handles medications prescribed inside treatments.
 */
@RestController
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationResponse> listMedications() {
        return medicationService.getAllMedications();
    }

    @GetMapping("/{medicationId}")
    public MedicationResponse getMedicationById(@PathVariable Long medicationId) {
        return medicationService.getMedicationById(medicationId);
    }

    @GetMapping("/treatment/{treatmentId}")
    public List<MedicationResponse> getMedicationsByTreatment(@PathVariable Long treatmentId) {
        return medicationService.getMedicationsByTreatmentId(treatmentId);
    }

    @GetMapping("/patient/{patientId}")
    public List<MedicationResponse> getMedicationsByPatient(@PathVariable Long patientId) {
        return medicationService.getMedicationsByPatientId(patientId);
    }

    @PutMapping("/{medicationId}")
    public MedicationResponse updateMedication(
            @PathVariable Long medicationId,
            @RequestBody MedicationRequest request) {
        return medicationService.updateMedication(medicationId, request);
    }

    @DeleteMapping("/{medicationId}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long medicationId) {
        medicationService.deleteMedication(medicationId);
        return ResponseEntity.noContent().build();
    }
}
