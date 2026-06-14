package com.vitalid.dtos.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vitalid.models.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(type = "string", example = "10:30", pattern = "HH:mm")
    private LocalTime time;

    private String reason;

    @Schema(example = "IN_PERSON", allowableValues = {"IN_PERSON", "VIDEO_CALL"})
    private AppointmentType appointmentType;

    private String status;
}
