package com.vitalid.services;

import com.vitalid.dtos.patient.PatientNoteRequest;
import com.vitalid.dtos.patient.PatientNoteResponse;
import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import com.vitalid.models.PatientNote;
import com.vitalid.repositories.PatientNoteRepository;
import com.vitalid.repositories.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class PatientNoteService {

    private final PatientNoteRepository patientNoteRepository;
    private final PatientRepository patientRepository;
    private final ResourceAccessService resourceAccessService;

    public PatientNoteService(PatientNoteRepository patientNoteRepository,
                              PatientRepository patientRepository,
                              ResourceAccessService resourceAccessService) {
        this.patientNoteRepository = patientNoteRepository;
        this.patientRepository = patientRepository;
        this.resourceAccessService = resourceAccessService;
    }

    public List<PatientNoteResponse> listByPatient(Long patientId) {
        Doctor doctor = resourceAccessService.currentDoctor();
        ensurePatientExists(patientId);
        return patientNoteRepository
                .findByDoctorIdAndPatientIdOrderByCreatedAtDesc(doctor.getId(), patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PatientNoteResponse create(PatientNoteRequest request) {
        Doctor doctor = resourceAccessService.currentDoctor();
        Patient patient = ensurePatientExists(requiredPatientId(request));

        PatientNote note = new PatientNote();
        note.setDoctor(doctor);
        note.setPatient(patient);
        note.setTitle(normalizeTitle(request.title()));
        note.setContent(normalizeContent(request.content()));

        return toResponse(patientNoteRepository.save(note));
    }

    public void delete(Long noteId) {
        Doctor doctor = resourceAccessService.currentDoctor();
        PatientNote note = patientNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient note not found"));
        if (!Objects.equals(note.getDoctor().getId(), doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patient note access denied");
        }
        patientNoteRepository.delete(note);
    }

    private Patient ensurePatientExists(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    private Long requiredPatientId(PatientNoteRequest request) {
        if (request == null || request.patientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient id is required");
        }
        return request.patientId();
    }

    private String normalizeTitle(String title) {
        String normalized = title == null ? "" : title.trim();
        if (normalized.isBlank()) {
            return "Nota clinica";
        }
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private String normalizeContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note content is required");
        }
        return normalized;
    }

    private PatientNoteResponse toResponse(PatientNote note) {
        return new PatientNoteResponse(
                note.getId(),
                note.getDoctor().getId(),
                note.getPatient().getId(),
                note.getPatient().getUser().getName(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
