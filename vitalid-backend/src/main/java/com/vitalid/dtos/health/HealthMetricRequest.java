package com.vitalid.dtos.health;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de registrar mÃ©trica de salud
 * 
 * TODO: Implement HealthMetricRequest with:
 * - metric (BLOOD_PRESSURE, BLOOD_SUGAR, WEIGHT, HEART_RATE)
 * - value (numeric value)
 * - timestamp (when measurement was taken)
 */
@Data
@NoArgsConstructor
public class HealthMetricRequest {
    private Long patientId;
    private String metric;
    private Double value;
    private String unit;
    private String timestamp;
    private String notes;

}



