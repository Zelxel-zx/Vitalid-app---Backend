package com.vitalid.repositories;

import com.vitalid.models.ScheduledTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduledTimeRepository extends JpaRepository<ScheduledTime, Long> {

    List<ScheduledTime> findByMedicationId(Long medicationId);
    void deleteByMedicationId(Long medicationId);
}
