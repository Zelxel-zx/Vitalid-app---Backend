package com.vitalid.controllers;

import com.vitalid.models.Checklist;
import com.vitalid.models.DosageRecord;
import com.vitalid.models.ScheduledTime;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.ChecklistRepository;
import com.vitalid.repositories.DosageRecordRepository;
import com.vitalid.models.Medication;
import com.vitalid.repositories.MedicationRepository;
import com.vitalid.models.Patient;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Checklist Controller
 * Handles medication adherence checklists and dosage records
 */
@RestController
@RequestMapping("/checklist")
public class ChecklistController {

    private final ChecklistRepository checklistRepository;
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final DosageRecordRepository dosageRecordRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChecklistController(ChecklistRepository checklistRepository,
                               MedicationRepository medicationRepository,
                               PatientRepository patientRepository,
                               DosageRecordRepository dosageRecordRepository,
                               UserRepository userRepository) {
        this.checklistRepository = checklistRepository;
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.dosageRecordRepository = dosageRecordRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<CreateChecklistResponse> createChecklist(@RequestBody ChecklistRequest request) {
        int added = 0;
        for (ChecklistMedication item : request.medications()) {
            Medication medication = medicationRepository.findById(item.medicationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found: " + item.medicationId()));
            Checklist checklist = new Checklist();
            checklist.setMedication(medication);
            checklist.setPatient(medication.getPatient());
            checklist.setScheduledTimes(new ArrayList<>());
            checklist.setDosageRecords(new ArrayList<>());
            if (item.scheduledTimes() != null) {
                List<ScheduledTime> schedule = new ArrayList<>();
                for (String time : item.scheduledTimes()) {
                    ScheduledTime scheduledTime = new ScheduledTime();
                    scheduledTime.setChecklist(checklist);
                    scheduledTime.setTime(time);
                    schedule.add(scheduledTime);
                }
                checklist.setScheduledTimes(schedule);
            }
            checklistRepository.save(checklist);
            added++;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateChecklistResponse(added, "Checklist items created"));
    }

    @GetMapping("/today")
    public List<ChecklistTodayResponse> getChecklistForToday() {
        return checklistRepository.findAll().stream().map(this::toTodayResponse).toList();
    }

    @PostMapping("/{medicationId}/mark-taken")
    public ResponseEntity<MessageResponse> markTaken(@PathVariable Long medicationId, @RequestBody ChecklistMarkTakenRequest request) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));

        validatePatientOwnsMedication(medication);

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
        record.setActualTime(request.timestamp());
        record.setIsTaken(true);
        record.setTimestamp(timestamp);
        dosageRecordRepository.save(record);

        if (medication.getPillsRemaining() != null && medication.getPillsRemaining() > 0) {
            medication.setPillsRemaining(medication.getPillsRemaining() - 1);
            medicationRepository.save(medication);
        }

        return ResponseEntity.ok(new MessageResponse("Dosage marked as taken"));
    }

    @PutMapping("/{medicationId}/side-effects")
    public ResponseEntity<MedicationResponse> addSideEffect(@PathVariable Long medicationId, @RequestBody SideEffectRequest request) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));

        validatePatientOwnsMedication(medication);

        String currentEffects = medication.getSideEffects() == null ? "" : medication.getSideEffects();
        if (!currentEffects.isEmpty()) {
            currentEffects += ", ";
        }
        currentEffects += request.effect();
        medication.setSideEffects(currentEffects);

        Medication saved = medicationRepository.save(medication);
        return ResponseEntity.ok(toMedicationResponse(saved));
    }

    private void validatePatientOwnsMedication(Medication medication) {
        User currentUser = getAuthenticatedUser();
        if (currentUser.getType() != UserType.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can update checklist intake");
        }

        Long medicationPatientUserId = medication.getPatient().getUser().getId();
        if (!Objects.equals(medicationPatientUserId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Medication does not belong to the authenticated patient");
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private Checklist createChecklistForMedication(Medication medication) {
        Checklist checklist = new Checklist();
        checklist.setMedication(medication);
        checklist.setPatient(medication.getPatient());
        checklist.setScheduledTimes(new ArrayList<>());
        checklist.setDosageRecords(new ArrayList<>());
        return checklistRepository.save(checklist);
    }

    private ChecklistTodayResponse toTodayResponse(Checklist checklist) {
        List<ScheduleItem> schedule = new ArrayList<>();
        if (checklist.getScheduledTimes() != null) {
            for (ScheduledTime scheduledTime : checklist.getScheduledTimes()) {
                boolean taken = false;
                String actualTime = null;
                if (checklist.getDosageRecords() != null) {
                    Optional<DosageRecord> takenRecord = checklist.getDosageRecords().stream()
                            .filter(d -> d.getScheduledTime() != null && d.getScheduledTime().equals(scheduledTime.getTime()) && Boolean.TRUE.equals(d.getIsTaken()))
                            .findFirst();
                    if (takenRecord.isPresent()) {
                        taken = true;
                        actualTime = takenRecord.get().getActualTime();
                    }
                }
                schedule.add(new ScheduleItem(scheduledTime.getTime(), taken, actualTime));
            }
        }
        return new ChecklistTodayResponse(
                checklist.getId(),
                checklist.getMedication().getId(),
                checklist.getMedication().getName(),
                checklist.getMedication().getDosage(),
                schedule
        );
    }

    private MedicationResponse toMedicationResponse(Medication medication) {
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

    public record ChecklistRequest(List<ChecklistMedication> medications) {
    }

    public record ChecklistMedication(Long medicationId, List<String> scheduledTimes) {
    }

    public record ChecklistTodayResponse(Long checklistId, Long medicationId, String medicationName, String dosage, List<ScheduleItem> scheduledTimes) {
    }

    public record ScheduleItem(String time, boolean taken, String actualTime) {
    }

    public record ChecklistMarkTakenRequest(String time, String timestamp) {
    }

    public record SideEffectRequest(String effect) {
    }

    public record MedicationResponse(Long id, String name, String dosage, String frequency, String prescribedBy, LocalDate startDate, LocalDate endDate, Integer totalPills, Integer pillsRemaining, String sideEffects) {
    }

    public record CreateChecklistResponse(int medicationsAdded, String message) {
    }

    public record MessageResponse(String message) {
    }
}



