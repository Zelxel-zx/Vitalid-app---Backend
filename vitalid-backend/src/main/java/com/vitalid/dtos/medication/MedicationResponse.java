package com.vitalid.dtos.medication;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Medication response with treatment context and schedule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse {

    private Long id;
    private Long treatmentId;
    private Long patientId;
    private Long doctorId;
    private String name;
    private String dosage;
    private String frequency;
    private String prescribedBy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private String unitType;
    private Integer pillsRemaining;
    private Integer lowStockThreshold;
    private String sideEffects;
    private List<String> scheduledTimes;
}



