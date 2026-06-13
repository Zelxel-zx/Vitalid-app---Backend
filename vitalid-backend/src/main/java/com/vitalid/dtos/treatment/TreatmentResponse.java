package com.vitalid.dtos.treatment;

import com.vitalid.dtos.medication.MedicationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentResponse {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private String doctorName;
    private String title;
    private String description;
    private String status;
    private Integer progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextAppointment;
    private List<MedicationResponse> medications;
}
