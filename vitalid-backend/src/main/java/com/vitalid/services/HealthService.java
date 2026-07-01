package com.vitalid.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.vitalid.dtos.health.HealthMetricRequest;
import com.vitalid.models.HealthMetric;
import com.vitalid.repositories.HealthMetricRepository;
import com.vitalid.repositories.PatientRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Health Service
 * Handles health metrics tracking and management
 * 
 * TODO: Implement methods:
 * - getBloodPressureHistory() -> List<BloodPressureResponse>
 * - getBloodSugarHistory() -> List<BloodSugarResponse>
 * - recordHealthMetric(HealthMetricRequest) -> void
 * - getHealthHistory(String metric, String from, String to) -> List<HealthMetricResponse>
 * - getLatestMetric(String metric) -> HealthMetricResponse
 * - getMetricsByDateRange(String metric, LocalDateTime from, LocalDateTime to) -> List<HealthMetricResponse>
 */
@Service
public class HealthService {

    @Autowired
    private HealthMetricRepository healthMetricRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<HealthMetric> getMetricsByPatientAndMetric(Long patientId, String metric) {
        if (patientId == null || metric == null || metric.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient and metric are required");
        }
        return healthMetricRepository.findByPatientIdAndMetric(patientId, metric.trim().toUpperCase());
    }

    public HealthMetric recordHealthMetric(HealthMetricRequest request) {
        if (request.getPatientId() == null || request.getMetric() == null || request.getValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient, metric, and value are required");
        }

        var patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        HealthMetric metric = new HealthMetric();
        metric.setPatient(patient);
        metric.setMetric(request.getMetric().trim().toUpperCase());
        metric.setValue(request.getValue());
        metric.setUnit(request.getUnit());
        metric.setNotes(request.getNotes());

        LocalDateTime timestamp = LocalDateTime.now();
        if (request.getTimestamp() != null && !request.getTimestamp().trim().isEmpty()) {
            try {
                timestamp = LocalDateTime.parse(request.getTimestamp());
            } catch (DateTimeParseException ignored) {
            }
        }
        metric.setTimestamp(timestamp);

        return healthMetricRepository.save(metric);
    }

    public List<HealthMetric> getHealthHistory(Long patientId, String metric, LocalDateTime from, LocalDateTime to) {
        if (patientId == null || metric == null || metric.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient and metric are required");
        }

        String metricKey = metric.trim().toUpperCase();
        if (from != null && to != null) {
            return healthMetricRepository.findByPatientIdAndMetricAndTimestampBetween(patientId, metricKey, from, to);
        }

        return healthMetricRepository.findByPatientIdAndMetric(patientId, metricKey);
    }

}



