package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkDoseTakenResponse {

    private Long medicationId;
    private LocalDate scheduledDate;
    private String scheduledTime;
    private LocalDateTime takenAt;
    private Integer pillsRemaining;
    private Boolean lowStock;
    private Integer treatmentProgress;
}
