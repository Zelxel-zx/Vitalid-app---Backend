package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistResponse {

    private Long treatmentId;
    private String treatmentTitle;
    private LocalDate date;
    private Integer progress;
    private ChecklistSummaryResponse summary;
    private List<ChecklistMedicationResponse> medications;
}
