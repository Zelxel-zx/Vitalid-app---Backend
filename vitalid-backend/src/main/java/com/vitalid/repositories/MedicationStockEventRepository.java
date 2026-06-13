package com.vitalid.repositories;

import com.vitalid.models.MedicationStockEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationStockEventRepository extends JpaRepository<MedicationStockEvent, Long> {

    List<MedicationStockEvent> findByMedicationIdOrderByCreatedAtDesc(Long medicationId);
}
