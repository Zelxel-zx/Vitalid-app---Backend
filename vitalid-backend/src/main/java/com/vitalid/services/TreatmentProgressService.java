package com.vitalid.services;

import com.vitalid.models.DosageRecord;
import com.vitalid.models.Medication;
import com.vitalid.models.ScheduledTime;
import com.vitalid.models.Treatment;
import com.vitalid.repositories.DosageRecordRepository;
import com.vitalid.repositories.MedicationRepository;
import com.vitalid.repositories.ScheduledTimeRepository;
import com.vitalid.repositories.TreatmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TreatmentProgressService {

    private final TreatmentRepository treatmentRepository;
    private final MedicationRepository medicationRepository;
    private final ScheduledTimeRepository scheduledTimeRepository;
    private final DosageRecordRepository dosageRecordRepository;

    public TreatmentProgressService(TreatmentRepository treatmentRepository,
                                    MedicationRepository medicationRepository,
                                    ScheduledTimeRepository scheduledTimeRepository,
                                    DosageRecordRepository dosageRecordRepository) {
        this.treatmentRepository = treatmentRepository;
        this.medicationRepository = medicationRepository;
        this.scheduledTimeRepository = scheduledTimeRepository;
        this.dosageRecordRepository = dosageRecordRepository;
    }

    @Transactional
    public int recalculate(Long treatmentId) {
        Treatment treatment = treatmentRepository.findById(treatmentId).orElse(null);
        if (treatment == null) {
            return 0;
        }

        List<Medication> medications = medicationRepository.findByTreatmentId(treatmentId);
        long scheduledDoses = 0;
        Map<Long, Set<String>> validTimesByMedication = new HashMap<>();
        for (Medication medication : medications) {
            List<ScheduledTime> times = scheduledTimeRepository.findByMedicationId(medication.getId());
            Set<String> validTimes = new HashSet<>();
            for (ScheduledTime time : times) {
                validTimes.add(time.getTime());
            }
            validTimesByMedication.put(medication.getId(), validTimes);
            if (times.isEmpty()) {
                continue;
            }
            LocalDate start = effectiveStart(treatment, medication);
            LocalDate end = effectiveEnd(treatment, medication);
            if (end.isBefore(start)) {
                continue;
            }
            scheduledDoses += (ChronoUnit.DAYS.between(start, end) + 1) * times.size();
        }

        if (scheduledDoses == 0) {
            treatment.setProgress(0);
            treatmentRepository.save(treatment);
            return 0;
        }

        Set<String> takenDoseKeys = new HashSet<>();
        for (DosageRecord record : dosageRecordRepository.findByTreatmentId(treatmentId)) {
            if (!Boolean.TRUE.equals(record.getIsTaken())
                    || record.getScheduledDate() == null
                    || record.getScheduledTime() == null
                    || record.getMedication() == null) {
                continue;
            }
            Medication medication = record.getMedication();
            if (!validTimesByMedication
                    .getOrDefault(medication.getId(), Set.of())
                    .contains(record.getScheduledTime())) {
                continue;
            }
            LocalDate start = effectiveStart(treatment, medication);
            LocalDate end = effectiveEnd(treatment, medication);
            if (record.getScheduledDate().isBefore(start) || record.getScheduledDate().isAfter(end)) {
                continue;
            }
            takenDoseKeys.add(medication.getId() + "|" + record.getScheduledDate()
                    + "|" + record.getScheduledTime());
        }

        int progress = (int) Math.round((takenDoseKeys.size() * 100.0) / scheduledDoses);
        progress = Math.max(0, Math.min(100, progress));
        treatment.setProgress(progress);
        treatmentRepository.save(treatment);
        return progress;
    }

    private LocalDate effectiveStart(Treatment treatment, Medication medication) {
        LocalDate start = latest(treatment.getStartDate(), medication.getStartDate());
        if (start != null) {
            return start;
        }
        if (treatment.getCreatedAt() != null) {
            return treatment.getCreatedAt().toLocalDate();
        }
        return LocalDate.now();
    }

    private LocalDate effectiveEnd(Treatment treatment, Medication medication) {
        LocalDate end = earliest(treatment.getEndDate(), medication.getEndDate());
        return end == null ? LocalDate.now() : end;
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
