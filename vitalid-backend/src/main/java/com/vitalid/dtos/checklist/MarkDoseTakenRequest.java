package com.vitalid.dtos.checklist;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MarkDoseTakenRequest {

    private String scheduledTime;
    private LocalDate scheduledDate;
    private LocalDateTime takenAt;
}
