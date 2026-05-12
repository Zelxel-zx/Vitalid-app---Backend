package com.vitalid.health.controller;

import com.vitalid.health.dto.HealthMetricRequest;
import com.vitalid.health.dto.BloodPressureResponse;
import com.vitalid.health.dto.BloodSugarResponse;
import com.vitalid.health.entity.HealthMetric;
import com.vitalid.health.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Health Metrics Controller
 * Handles health measurements and vital signs tracking
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    @GetMapping("/blood-pressure")
    public List<BloodPressureResponse> getBloodPressure(@RequestParam("patientId") Long patientId) {
        List<HealthMetric> metrics = healthService.getMetricsByPatientAndMetric(patientId, "PRESSURE");
        if (metrics.isEmpty()) {
            metrics = healthService.getMetricsByPatientAndMetric(patientId, "BLOOD_PRESSURE");
        }
        return metrics.stream().map(metric -> {
            BloodPressureResponse response = new BloodPressureResponse();
            response.setDate(metric.getTimestamp().toString());
            response.setValue(metric.getValue());
            return response;
        }).toList();
    }

    @GetMapping("/blood-sugar")
    public List<BloodSugarResponse> getBloodSugar(@RequestParam("patientId") Long patientId) {
        List<HealthMetric> metrics = healthService.getMetricsByPatientAndMetric(patientId, "GLUCOSE");
        if (metrics.isEmpty()) {
            metrics = healthService.getMetricsByPatientAndMetric(patientId, "BLOOD_SUGAR");
        }
        return metrics.stream().map(metric -> {
            BloodSugarResponse response = new BloodSugarResponse();
            response.setDate(metric.getTimestamp().toString());
            response.setValue(metric.getValue());
            return response;
        }).toList();
    }

    @PostMapping("/metrics")
    public ResponseEntity<MessageResponse> recordMetric(@RequestBody HealthMetricRequest request) {
        healthService.recordHealthMetric(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Health metric recorded"));
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory(
            @RequestParam("patientId") Long patientId,
            @RequestParam("metric") String metric,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        LocalDateTime fromDate = parseDateTime(from);
        LocalDateTime toDate = parseDateTime(to);

        return healthService.getHealthHistory(patientId, metric, fromDate, toDate).stream()
                .map(item -> new HistoryResponse(item.getTimestamp().toString(), item.getValue()))
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public record MessageResponse(String message) {
    }

    public record HistoryResponse(String date, Double value) {
    }

}

