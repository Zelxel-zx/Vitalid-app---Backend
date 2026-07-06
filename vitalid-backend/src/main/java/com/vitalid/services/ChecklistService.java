package com.vitalid.services;

import com.vitalid.dtos.checklist.*;
import com.vitalid.models.*;
import com.vitalid.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChecklistService {

    private final TreatmentRepository treatmentRepository;
    private final MedicationRepository medicationRepository;
    private final ScheduledTimeRepository scheduledTimeRepository;
    private final ChecklistRepository checklistRepository;
    private final DosageRecordRepository dosageRecordRepository;
    private final MedicationStockEventRepository stockEventRepository;
    private final ResourceAccessService resourceAccessService;
    private final TreatmentProgressService progressService;

    public ChecklistService(TreatmentRepository treatmentRepository,
                            MedicationRepository medicationRepository,
                            ScheduledTimeRepository scheduledTimeRepository,
                            ChecklistRepository checklistRepository,
                            DosageRecordRepository dosageRecordRepository,
                            MedicationStockEventRepository stockEventRepository,
                            ResourceAccessService resourceAccessService,
                            TreatmentProgressService progressService) {
        this.treatmentRepository = treatmentRepository;
        this.medicationRepository = medicationRepository;
        this.scheduledTimeRepository = scheduledTimeRepository;
        this.checklistRepository = checklistRepository;
        this.dosageRecordRepository = dosageRecordRepository;
        this.stockEventRepository = stockEventRepository;
        this.resourceAccessService = resourceAccessService;
        this.progressService = progressService;
    }

    public ChecklistResponse getTreatmentChecklist(Long treatmentId, LocalDate requestedDate) {
        Treatment treatment = findTreatment(treatmentId);
        resourceAccessService.requireTreatmentReadAccess(treatment);
        LocalDate date = requestedDate == null ? LocalDate.now() : requestedDate;

        List<DosageRecord> records =
                dosageRecordRepository.findByTreatmentIdAndScheduledDate(treatmentId, date);
        List<ChecklistMedicationResponse> medicationResponses = new ArrayList<>();
        int scheduled = 0;
        int taken = 0;
        int missed = 0;

        for (Medication medication : medicationRepository.findByTreatmentId(treatmentId)) {
            List<ScheduledDoseResponse> doses = new ArrayList<>();
            if (isActiveOn(medication, treatment, date)) {
                for (ScheduledTime scheduledTime :
                        scheduledTimeRepository.findByMedicationId(medication.getId())) {
                    Optional<DosageRecord> record = records.stream()
                            .filter(item -> Objects.equals(item.getMedication().getId(), medication.getId())
                                    && Objects.equals(item.getScheduledTime(), scheduledTime.getTime())
                                    && Boolean.TRUE.equals(item.getIsTaken()))
                            .findFirst();
                    doses.add(new ScheduledDoseResponse(
                            scheduledTime.getTime(),
                            record.isPresent(),
                            record.map(DosageRecord::getTimestamp).orElse(null)));
                    scheduled++;
                    if (record.isPresent()) {
                        taken++;
                    } else if (isDoseElapsed(date, scheduledTime.getTime())) {
                        missed++;
                    }
                }
            }

            medicationResponses.add(new ChecklistMedicationResponse(
                    medication.getId(),
                    medication.getName(),
                    medication.getDosage(),
                    medication.getInstructions(),
                    medication.getStartDate(),
                    medication.getEndDate(),
                    medication.getPillsRemaining(),
                    medication.getLowStockThreshold(),
                    isLowStock(medication),
                    medication.getSideEffects(),
                    doses));
        }

        int percentage = scheduled == 0 ? 0 : (int) Math.round(taken * 100.0 / scheduled);
        ChecklistSummaryResponse summary = new ChecklistSummaryResponse(
                medicationResponses.size(),
                scheduled,
                taken,
                scheduled - taken,
                missed,
                percentage);
        return new ChecklistResponse(
                treatment.getId(),
                treatment.getTitle(),
                date,
                treatment.getStartDate(),
                treatment.getEndDate(),
                treatment.getProgress(),
                summary,
                medicationResponses);
    }

    private boolean isDoseElapsed(LocalDate date, String scheduledTime) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return true;
        }
        if (date.isAfter(today)) {
            return false;
        }
        return LocalTime.parse(scheduledTime).isBefore(LocalTime.now());
    }

    @Transactional
    public MarkDoseTakenResponse markTaken(Long medicationId, MarkDoseTakenRequest request) {
        Medication medication = findPatientMedication(medicationId);
        validateMarkRequest(medication, request);
        LocalDate scheduledDate =
                request.getScheduledDate() == null ? LocalDate.now() : request.getScheduledDate();
        LocalDateTime takenAt =
                request.getTakenAt() == null ? LocalDateTime.now() : request.getTakenAt();

        Optional<DosageRecord> existing =
                dosageRecordRepository.findFirstByMedicationIdAndScheduledDateAndScheduledTime(
                        medicationId, scheduledDate, request.getScheduledTime());

        DosageRecord record;
        boolean newlyTaken = existing.isEmpty() || !Boolean.TRUE.equals(existing.get().getIsTaken());
        if (existing.isPresent()) {
            record = existing.get();
        } else {
            record = new DosageRecord();
            record.setChecklist(getOrCreateChecklist(medication.getTreatment()));
            record.setMedication(medication);
            record.setTreatment(medication.getTreatment());
            record.setPatient(medication.getPatient());
            record.setScheduledDate(scheduledDate);
            record.setScheduledTime(request.getScheduledTime());
        }
        record.setActualTime(takenAt.toLocalTime().withNano(0).toString());
        record.setTimestamp(takenAt);
        record.setIsTaken(true);
        dosageRecordRepository.save(record);

        if (newlyTaken && medication.getPillsRemaining() != null
                && medication.getPillsRemaining() > 0) {
            medication.setPillsRemaining(medication.getPillsRemaining() - 1);
            medicationRepository.save(medication);
        }

        int progress = progressService.recalculate(medication.getTreatment().getId());
        return new MarkDoseTakenResponse(
                medication.getId(),
                scheduledDate,
                request.getScheduledTime(),
                takenAt,
                medication.getPillsRemaining(),
                isLowStock(medication),
                progress);
    }

    @Transactional
    public DoseRevertedResponse revertTaken(
            Long medicationId, LocalDate requestedDate, String requestedTime) {
        Medication medication = findPatientMedication(medicationId);
        MarkDoseTakenRequest validationRequest = new MarkDoseTakenRequest();
        validationRequest.setScheduledDate(requestedDate);
        validationRequest.setScheduledTime(requestedTime);
        validateMarkRequest(medication, validationRequest);

        LocalDate scheduledDate = requestedDate == null ? LocalDate.now() : requestedDate;
        DosageRecord record =
                dosageRecordRepository.findFirstByMedicationIdAndScheduledDateAndScheduledTime(
                                medicationId, scheduledDate, validationRequest.getScheduledTime())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Taken dosage record not found"));

        if (Boolean.TRUE.equals(record.getIsTaken())) {
            record.setIsTaken(false);
            record.setActualTime(null);
            dosageRecordRepository.save(record);

            if (medication.getPillsRemaining() != null) {
                medication.setPillsRemaining(medication.getPillsRemaining() + 1);
                medicationRepository.save(medication);
            }
        }

        int progress = progressService.recalculate(medication.getTreatment().getId());
        return new DoseRevertedResponse(
                medication.getId(),
                scheduledDate,
                validationRequest.getScheduledTime(),
                medication.getPillsRemaining(),
                isLowStock(medication),
                progress);
    }

    @Transactional
    public StockUpdateResponse addPurchasedStock(Long medicationId, StockUpdateRequest request) {
        Medication medication = findPatientMedication(medicationId);
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Purchased quantity must be greater than zero");
        }

        int currentRemaining = Optional.ofNullable(medication.getPillsRemaining()).orElse(0);
        try {
            medication.setPillsRemaining(Math.addExact(currentRemaining, request.getQuantity()));
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity is too large");
        }
        medicationRepository.save(medication);

        MedicationStockEvent event = new MedicationStockEvent();
        event.setMedication(medication);
        event.setPatient(medication.getPatient());
        event.setQuantity(request.getQuantity());
        event.setEventType("PURCHASE");
        event.setNotes(request.getNotes());
        stockEventRepository.save(event);

        return new StockUpdateResponse(
                medication.getId(),
                request.getQuantity(),
                medication.getPillsRemaining(),
                isLowStock(medication));
    }

    @Transactional
    public SideEffectResponse addSideEffect(Long medicationId, SideEffectRequest request) {
        Medication medication = findPatientMedication(medicationId);
        if (request == null || request.getEffect() == null || request.getEffect().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Side effect is required");
        }

        String effect = request.getEffect().trim();
        String current = Optional.ofNullable(medication.getSideEffects()).orElse("").trim();
        String updated = current.isEmpty() ? effect : current + "; " + effect;
        if (updated.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Side effects cannot exceed 1000 characters");
        }
        medication.setSideEffects(updated);
        medicationRepository.save(medication);
        return new SideEffectResponse(medication.getId(), updated);
    }

    private Treatment findTreatment(Long treatmentId) {
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Treatment not found"));
    }

    private Medication findPatientMedication(Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medication not found"));
        if (!resourceAccessService.isAuthenticated()) {
            return medication;
        }
        Patient patient = resourceAccessService.currentPatient();
        if (!Objects.equals(medication.getPatient().getId(), patient.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Medication access denied");
        }
        return medication;
    }

    private Checklist getOrCreateChecklist(Treatment treatment) {
        return checklistRepository.findFirstByTreatmentId(treatment.getId())
                .orElseGet(() -> {
                    Checklist checklist = new Checklist();
                    checklist.setTreatment(treatment);
                    checklist.setPatient(treatment.getPatient());
                    checklist.setMedication(null);
                    checklist.setScheduledTimes(new ArrayList<>());
                    checklist.setDosageRecords(new ArrayList<>());
                    return checklistRepository.save(checklist);
                });
    }

    private void validateMarkRequest(Medication medication, MarkDoseTakenRequest request) {
        if (request == null || request.getScheduledTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled time is required");
        }
        String time = request.getScheduledTime().trim();
        try {
            LocalTime.parse(time);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Scheduled time must use HH:mm format");
        }
        boolean scheduled = scheduledTimeRepository.findByMedicationId(medication.getId()).stream()
                .anyMatch(item -> item.getTime().equals(time));
        if (!scheduled) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Time is not scheduled for this medication");
        }
        request.setScheduledTime(time);

        LocalDate date = request.getScheduledDate() == null ? LocalDate.now() : request.getScheduledDate();
        if (date.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Future doses cannot be marked as taken");
        }
        if (request.getTakenAt() != null && request.getTakenAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Taken time cannot be in the future");
        }
        if (!isActiveOn(medication, medication.getTreatment(), date)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Medication is not active on the scheduled date");
        }
    }

    private boolean isActiveOn(Medication medication, Treatment treatment, LocalDate date) {
        LocalDate start = latest(treatment.getStartDate(), medication.getStartDate());
        LocalDate end = earliest(treatment.getEndDate(), medication.getEndDate());
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    private boolean isLowStock(Medication medication) {
        return medication.getPillsRemaining() != null
                && medication.getLowStockThreshold() != null
                && medication.getPillsRemaining() <= medication.getLowStockThreshold();
    }

    private LocalDate latest(LocalDate first, LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first : second;
    }

    private LocalDate earliest(LocalDate first, LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isBefore(second) ? first : second;
    }
}
