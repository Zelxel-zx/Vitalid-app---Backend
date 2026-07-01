package com.vitalid.repositories;

import com.vitalid.models.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HealthMetric Repository
 * Data access for HealthMetric entity
 */
@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {

    List<HealthMetric> findByPatientId(Long patientId);
    List<HealthMetric> findByPatientIdAndMetric(Long patientId, String metric);
    List<HealthMetric> findByMetric(String metric);
    List<HealthMetric> findByPatientIdAndMetricAndTimestampBetween(Long patientId, String metric, LocalDateTime from, LocalDateTime to);
}



