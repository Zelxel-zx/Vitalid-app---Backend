package com.vitalid.dtos.medication;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Request to create or update a medication inside a treatment.
 */
@Data
@NoArgsConstructor
public class MedicationRequest {

    private String name;
    private String dosage;
    private String frequency;
    private String prescribedBy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private String unitType;
    private Integer totalPills;
    private Integer pillsRemaining;
    private Integer lowStockThreshold;
    private List<String> scheduledTimes;
}



