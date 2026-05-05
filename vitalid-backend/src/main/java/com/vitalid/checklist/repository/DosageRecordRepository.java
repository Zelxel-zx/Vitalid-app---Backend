package com.vitalid.checklist.repository;

import com.vitalid.checklist.entity.DosageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Dosage Record Repository
 * Data access for DosageRecord entity
 */
@Repository
public interface DosageRecordRepository extends JpaRepository<DosageRecord, Long> {

    List<DosageRecord> findByChecklistId(Long checklistId);
    List<DosageRecord> findByMedicationId(Long medicationId);
    List<DosageRecord> findByChecklistIdAndIsTakenFalse(Long checklistId);
}

