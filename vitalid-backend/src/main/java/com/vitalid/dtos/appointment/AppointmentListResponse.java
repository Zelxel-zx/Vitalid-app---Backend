package com.vitalid.dtos.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentListResponse {

    private List<AppointmentResponse> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
