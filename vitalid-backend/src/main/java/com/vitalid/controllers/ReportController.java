package com.vitalid.controllers;

import com.vitalid.services.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/patient/{patientId}/pdf")
    public ResponseEntity<byte[]> downloadPatientReport(@PathVariable Long patientId) {
        byte[] pdf = reportService.buildPatientReport(patientId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vitalid-report-" + patientId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/user/{userId}/pdf")
    public ResponseEntity<byte[]> downloadPatientReportForUser(@PathVariable Long userId) {
        byte[] pdf = reportService.buildPatientReportForUser(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vitalid-report-" + userId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}


