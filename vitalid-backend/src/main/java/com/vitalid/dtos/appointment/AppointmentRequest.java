package com.vitalid.dtos.appointment;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class AppointmentRequest {

    private Long patientId;
    private Long doctorId;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private String status;
}
