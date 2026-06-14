package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistMedicationResponse {

    private Long medicationId;
    private String name;
    private String dosage;
    private String instructions;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pillsRemaining;
    private Integer lowStockThreshold;
    private Boolean lowStock;
    private String sideEffects;
    private List<ScheduledDoseResponse> doses;
}
