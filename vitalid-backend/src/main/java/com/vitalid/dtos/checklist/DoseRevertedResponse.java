package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoseRevertedResponse {

    private Long medicationId;
    private LocalDate scheduledDate;
    private String scheduledTime;
    private Integer pillsRemaining;
    private Boolean lowStock;
    private Integer treatmentProgress;
}
