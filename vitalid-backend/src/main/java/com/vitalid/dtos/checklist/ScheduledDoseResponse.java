package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledDoseResponse {

    private String scheduledTime;
    private Boolean taken;
    private LocalDateTime takenAt;
}
