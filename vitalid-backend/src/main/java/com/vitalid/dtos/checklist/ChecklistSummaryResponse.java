package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistSummaryResponse {

    private Integer totalMedications;
    private Integer totalScheduledDoses;
    private Integer takenDoses;
    private Integer pendingDoses;
    private Integer missedDoses;
    private Integer percentage;
}
