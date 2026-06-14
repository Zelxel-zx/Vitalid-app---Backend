package com.vitalid.dtos.treatment;

import com.vitalid.dtos.medication.MedicationRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class TreatmentRequest {

    private Long patientId;
    private Long doctorId;
    private String title;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextAppointment;
    private List<MedicationRequest> medications;
}
