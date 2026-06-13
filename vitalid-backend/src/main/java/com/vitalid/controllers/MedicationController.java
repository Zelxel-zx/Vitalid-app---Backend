package com.vitalid.controllers;

import com.vitalid.models.Medication;
import com.vitalid.repositories.MedicationRepository;
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
 * Medication Controller
 * Handles medication and prescription management
 */
@RestController
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public MedicationController(MedicationRepository medicationRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository) {
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<MedicationResponse> listMedications() {
        return medicationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/patient/{userId}")
    public List<MedicationResponse> getMedicationsForPatient(@PathVariable Long userId) {
        return medicationRepository.findByPatient_User_Id(userId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<MedicationResponse> createMedication(@PathVariable Long userId, @RequestBody MedicationRequest request) {
        var patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        var doctor = request.doctorId() != null
                ? doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"))
                : doctorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No doctor available"));

        Medication medication = new Medication();
        medication.setPatient(patient);
        medication.setDoctor(doctor);
        medication.setName(request.name());
        medication.setDosage(request.dosage());
        medication.setFrequency(request.frequency());
        medication.setPrescribedBy(request.prescribedBy() == null ? doctor.getUser().getName() : request.prescribedBy());
        medication.setStartDate(request.startDate());
        medication.setEndDate(request.endDate());
        medication.setTotalPills(request.totalPills() != null ? request.totalPills() : 30);
        medication.setPillsRemaining(request.totalPills() != null ? request.totalPills() : 30);
        medication.setSideEffects("");

        Medication saved = medicationRepository.save(medication);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    private MedicationResponse toResponse(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getPrescribedBy(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.getTotalPills(),
                medication.getPillsRemaining(),
                medication.getSideEffects()
        );
    }

    public record MedicationRequest(Long patientId, Long doctorId, String name, String dosage, String frequency, String prescribedBy, LocalDate startDate, LocalDate endDate, Integer totalPills) {
    }

    public record MedicationResponse(Long id, String name, String dosage, String frequency, String prescribedBy, LocalDate startDate, LocalDate endDate, Integer totalPills, Integer pillsRemaining, String sideEffects) {
    }

}



