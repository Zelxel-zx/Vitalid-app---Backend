package com.vitalid.controllers;

import com.vitalid.dtos.patient.PatientNoteRequest;
import com.vitalid.dtos.patient.PatientNoteResponse;
import com.vitalid.services.PatientNoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patient-notes")
public class PatientNoteController {

    private final PatientNoteService patientNoteService;

    public PatientNoteController(PatientNoteService patientNoteService) {
        this.patientNoteService = patientNoteService;
    }

    @GetMapping("/patient/{patientId}")
    public List<PatientNoteResponse> listByPatient(@PathVariable Long patientId) {
        return patientNoteService.listByPatient(patientId);
    }

    @PostMapping
    public ResponseEntity<PatientNoteResponse> create(@RequestBody PatientNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientNoteService.create(request));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId) {
        patientNoteService.delete(noteId);
        return ResponseEntity.noContent().build();
    }
}
