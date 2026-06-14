package com.vitalid.dtos.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private Long patientUserId;
    private Long doctorId;
    private Long doctorUserId;
    private String patientName;
    private String doctorName;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private String status;
}
