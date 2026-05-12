package com.vitalid.health.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de presión arterial
 * 
 * TODO: Implement BloodPressureResponse with:
 * - date (date of measurement)
 * - value (systolic/diastolic as single value or combined)
 * Or separate fields:
 * - systolic
 * - diastolic
 */
@Data
@NoArgsConstructor
public class BloodPressureResponse {
    private String date;
    private Double value;

}

