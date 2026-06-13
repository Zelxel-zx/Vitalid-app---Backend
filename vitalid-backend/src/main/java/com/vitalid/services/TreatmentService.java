package com.vitalid.services;

import com.vitalid.dtos.medication.MedicationRequest;
import com.vitalid.dtos.medication.MedicationResponse;
import com.vitalid.dtos.treatment.TreatmentRequest;
import com.vitalid.dtos.treatment.TreatmentResponse;
import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import com.vitalid.models.Treatment;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.TreatmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicationService medicationService;
    private final ResourceAccessService resourceAccessService;
    private final TreatmentProgressService progressService;

    public TreatmentService(TreatmentRepository treatmentRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            MedicationService medicationService,
                            ResourceAccessService resourceAccessService,
                            TreatmentProgressService progressService) {
        this.treatmentRepository = treatmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.medicationService = medicationService;
        this.resourceAccessService = resourceAccessService;
        this.progressService = progressService;
    }

    public List<TreatmentResponse> getAllTreatments() {
        if (!resourceAccessService.isAuthenticated()) {
            return treatmentRepository.findAll().stream().map(this::toResponse).toList();
        }
        User user = resourceAccessService.currentUser();
        List<Treatment> treatments = user.getType() == UserType.DOCTOR
                ? treatmentRepository.findByDoctor_User_Id(user.getId())
                : treatmentRepository.findByPatient_User_Id(user.getId());
        return treatments.stream().map(this::toResponse).toList();
    }

    public TreatmentResponse getTreatmentById(Long treatmentId) {
        Treatment treatment = findTreatment(treatmentId);
        resourceAccessService.requireTreatmentReadAccess(treatment);
        return toResponse(treatment);
    }

    public List<TreatmentResponse> getTreatmentsByStatus(String status) {
        validateStatus(status);
        if (!resourceAccessService.isAuthenticated()) {
            return treatmentRepository.findByStatus(status).stream().map(this::toResponse).toList();
        }
        User user = resourceAccessService.currentUser();
        List<Treatment> treatments;
        if (user.getType() == UserType.DOCTOR) {
            Doctor doctor = resourceAccessService.currentDoctor();
            treatments = treatmentRepository.findByDoctorIdAndStatus(doctor.getId(), status);
        } else {
            Patient patient = resourceAccessService.currentPatient();
            treatments = treatmentRepository.findByPatientIdAndStatus(patient.getId(), status);
        }
        return treatments.stream().map(this::toResponse).toList();
    }

    public List<TreatmentResponse> getTreatmentsByPatientId(Long patientId) {
        if (!resourceAccessService.isAuthenticated()) {
            return treatmentRepository.findByPatientId(patientId).stream().map(this::toResponse).toList();
        }
        User user = resourceAccessService.currentUser();
        List<Treatment> treatments;
        if (user.getType() == UserType.DOCTOR) {
            Doctor doctor = resourceAccessService.currentDoctor();
            treatments = treatmentRepository.findByPatientIdAndDoctorId(patientId, doctor.getId());
        } else {
            Patient patient = resourceAccessService.currentPatient();
            requireSameId(patientId, patient.getId(), "Patient access denied");
            treatments = treatmentRepository.findByPatientId(patientId);
        }
        return treatments.stream().map(this::toResponse).toList();
    }

    public List<TreatmentResponse> getTreatmentsByDoctorId(Long doctorId) {
        if (!resourceAccessService.isAuthenticated()) {
            return treatmentRepository.findByDoctorId(doctorId).stream().map(this::toResponse).toList();
        }
        Doctor doctor = resourceAccessService.currentDoctor();
        requireSameId(doctorId, doctor.getId(), "Doctor access denied");
        return treatmentRepository.findByDoctorId(doctorId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TreatmentResponse createTreatment(TreatmentRequest request) {
        Doctor authenticatedDoctor;
        if (resourceAccessService.isAuthenticated()) {
            authenticatedDoctor = resourceAccessService.currentDoctor();
        } else {
            authenticatedDoctor = doctorRepository
                    .findById(requiredId(request.getDoctorId(), "Doctor id is required"))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Doctor not found"));
        }
        Patient patient = patientRepository.findById(requiredId(request.getPatientId(), "Patient id is required"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (resourceAccessService.isAuthenticated() && request.getDoctorId() != null) {
            requireSameId(request.getDoctorId(), authenticatedDoctor.getId(), "Doctor access denied");
        }

        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setDoctor(authenticatedDoctor);
        treatment.setProgress(0);
        applyRequest(treatment, request, true);

        Treatment saved = treatmentRepository.save(treatment);
        createInitialMedications(saved.getId(), request.getMedications());

        return toResponse(saved);
    }

    @Transactional
    public TreatmentResponse updateTreatment(Long treatmentId, TreatmentRequest request) {
        Treatment treatment = findTreatment(treatmentId);
        resourceAccessService.requireTreatmentWriteAccess(treatment);
        applyRequest(treatment, request, false);
        Treatment saved = treatmentRepository.save(treatment);
        progressService.recalculate(saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteTreatment(Long treatmentId) {
        Treatment treatment = findTreatment(treatmentId);
        resourceAccessService.requireTreatmentWriteAccess(treatment);
        treatmentRepository.delete(treatment);
    }

    private Treatment findTreatment(Long treatmentId) {
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment not found"));
    }

    private void applyRequest(Treatment treatment, TreatmentRequest request, boolean create) {
        if (create || request.getTitle() != null) {
            treatment.setTitle(required(request.getTitle(), "Treatment title is required"));
        }
        if (create || request.getDescription() != null) {
            treatment.setDescription(request.getDescription());
        }
        if (create || request.getStatus() != null) {
            String status = Optional.ofNullable(request.getStatus()).orElse("ACTIVE").toUpperCase();
            validateStatus(status);
            treatment.setStatus(status);
        }
        if (create || request.getStartDate() != null) {
            treatment.setStartDate(request.getStartDate());
        }
        if (create || request.getEndDate() != null) {
            treatment.setEndDate(request.getEndDate());
        }
        if (create || request.getNextAppointment() != null) {
            treatment.setNextAppointment(request.getNextAppointment());
        }
        if (treatment.getProgress() == null) {
            treatment.setProgress(0);
        }
        validateDates(treatment.getStartDate(), treatment.getEndDate());
        treatment.setMedications(null);
    }

    private void createInitialMedications(Long treatmentId, List<MedicationRequest> medications) {
        if (medications == null || medications.isEmpty()) {
            return;
        }
        for (MedicationRequest medication : medications) {
            medicationService.createMedication(treatmentId, medication);
        }
    }

    private Long requiredId(Long value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Treatment end date cannot be before start date");
        }
    }

    private void validateStatus(String status) {
        if (!List.of("ACTIVE", "COMPLETED", "PENDING", "PAUSED", "CANCELLED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid treatment status");
        }
    }

    private void requireSameId(Long requestedId, Long authenticatedId, String message) {
        if (!java.util.Objects.equals(requestedId, authenticatedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private TreatmentResponse toResponse(Treatment treatment) {
        List<MedicationResponse> medications = medicationService.getMedicationsByTreatmentId(treatment.getId());
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getPatient().getId(),
                treatment.getDoctor().getId(),
                treatment.getPatient().getUser().getName(),
                treatment.getDoctor().getUser().getName(),
                treatment.getTitle(),
                treatment.getDescription(),
                treatment.getStatus(),
                treatment.getProgress(),
                treatment.getStartDate(),
                treatment.getEndDate(),
                treatment.getNextAppointment(),
                medications
        );
    }
}
