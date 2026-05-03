package com.vitalid.health.repository;

import com.vitalid.health.entity.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HealthMetric Repository
 * Data access for HealthMetric entity
 * 
 * TODO: Implement query methods:
 * - findByPatientId(String patientId)
 * - findByPatientIdAndMetric(String patientId, String metric)
 * - findByPatientIdAndMetricAndTimestampBetween(String patientId, String metric, LocalDateTime from, LocalDateTime to)
 * - findByMetric(String metric)
 * - findLatestByPatientIdAndMetric(String patientId, String metric)
 */
@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, String> {

    // TODO: Add custom query methods

}
