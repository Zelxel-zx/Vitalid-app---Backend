package com.vitalid.controllers;

import com.vitalid.dtos.checklist.*;
import com.vitalid.services.ChecklistService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping("/treatment/{treatmentId}")
    public ChecklistResponse getTreatmentChecklist(
            @PathVariable Long treatmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return checklistService.getTreatmentChecklist(treatmentId, date);
    }

    @PostMapping("/medications/{medicationId}/mark-taken")
    public ResponseEntity<MarkDoseTakenResponse> markTaken(
            @PathVariable Long medicationId,
            @RequestBody MarkDoseTakenRequest request) {
        return ResponseEntity.ok(checklistService.markTaken(medicationId, request));
    }

    @DeleteMapping("/medications/{medicationId}/mark-taken")
    public ResponseEntity<DoseRevertedResponse> revertTaken(
            @PathVariable Long medicationId,
            @RequestParam String scheduledTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate) {
        return ResponseEntity.ok(
                checklistService.revertTaken(medicationId, scheduledDate, scheduledTime));
    }

    @PutMapping("/medications/{medicationId}/stock")
    public ResponseEntity<StockUpdateResponse> addPurchasedStock(
            @PathVariable Long medicationId,
            @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(checklistService.addPurchasedStock(medicationId, request));
    }

    @PutMapping("/medications/{medicationId}/side-effects")
    public ResponseEntity<SideEffectResponse> addSideEffect(
            @PathVariable Long medicationId,
            @RequestBody SideEffectRequest request) {
        return ResponseEntity.ok(checklistService.addSideEffect(medicationId, request));
    }
}
