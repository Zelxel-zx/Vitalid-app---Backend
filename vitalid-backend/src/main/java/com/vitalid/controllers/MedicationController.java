package com.vitalid.controllers;

import com.vitalid.models.Checklist;
import com.vitalid.repositories.ChecklistRepository;
import com.vitalid.repositories.DosageRecordRepository;
import com.vitalid.models.Medication;
import com.vitalid.repositories.MedicationRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.models.User;
import com.vitalid.repositories.UserRepository;
import com.vitalid.models.DosageRecord;
import com.vitalid.models.ScheduledTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Medication Controller
 * Handles medication and prescription management
 */
@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ChecklistRepository checklistRepository;
    private final DosageRecordRepository dosageRecordRepository;

    @Autowired
    public MedicationController(MedicationRepository medicationRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository,
                                ChecklistRepository checklistRepository,
                                DosageRecordRepository dosageRecordRepository) {
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.checklistRepository = checklistRepository;
        this.dosageRecordRepository = dosageRecordRepository;
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

    @PostMapping("/{medicationId}/take-dose")
    public ResponseEntity<MessageResponse> takeDose(@PathVariable Long medicationId, @RequestBody DoseRequest request) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));

        Checklist checklist = checklistRepository.findByMedicationId(medicationId).stream().findFirst()
                .orElseGet(() -> createChecklistForMedication(medication));

        LocalDateTime timestamp = LocalDateTime.now();
        if (request.timestamp() != null) {
            try {
                timestamp = LocalDateTime.parse(request.timestamp());
            } catch (DateTimeParseException ignored) {
            }
        }

        DosageRecord record = new DosageRecord();
        record.setChecklist(checklist);
        record.setMedication(medication);
        record.setScheduledTime(request.time());
        record.setActualTime(request.timestamp() != null ? request.timestamp() : null);
        record.setIsTaken(true);
        record.setTimestamp(timestamp);

        dosageRecordRepository.save(record);
        
        if (medication.getPillsRemaining() != null && medication.getPillsRemaining() > 0) {
            medication.setPillsRemaining(medication.getPillsRemaining() - 1);
            medicationRepository.save(medication);
        }
        
        return ResponseEntity.ok(new MessageResponse("Dose recorded successfully"));
    }

    @PutMapping("/{medicationId}/side-effects")
    public ResponseEntity<MedicationResponse> addSideEffect(@PathVariable Long medicationId, @RequestBody SideEffectRequest request) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
        
        String currentEffects = medication.getSideEffects() == null ? "" : medication.getSideEffects();
        if (!currentEffects.isEmpty()) {
            currentEffects += ", ";
        }
        currentEffects += request.effect();
        medication.setSideEffects(currentEffects);
        
        Medication saved = medicationRepository.save(medication);
        return ResponseEntity.ok(toResponse(saved));
    }

    private Checklist createChecklistForMedication(Medication medication) {
        Checklist checklist = new Checklist();
        checklist.setMedication(medication);
        checklist.setPatient(medication.getPatient());
        checklist.setScheduledTimes(new ArrayList<>());
        checklist.setDosageRecords(new ArrayList<>());
        return checklistRepository.save(checklist);
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

    public record DoseRequest(String time, String timestamp) {
    }

    public record SideEffectRequest(String effect) {
    }

    public record MessageResponse(String message) {
    }
}



