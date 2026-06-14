package com.vitalid.services;

import com.vitalid.dtos.medication.MedicationRequest;
import com.vitalid.dtos.medication.MedicationResponse;
import com.vitalid.models.Medication;
import com.vitalid.models.ScheduledTime;
import com.vitalid.models.Treatment;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.MedicationRepository;
import com.vitalid.repositories.ScheduledTimeRepository;
import com.vitalid.repositories.TreatmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final TreatmentRepository treatmentRepository;
    private final ScheduledTimeRepository scheduledTimeRepository;
    private final ResourceAccessService resourceAccessService;
    private final TreatmentProgressService progressService;
    private static final Pattern TIME_PATTERN =
            Pattern.compile("^(?:[01]\\d|2[0-3]):[0-5]\\d$");

    public MedicationService(MedicationRepository medicationRepository,
                             TreatmentRepository treatmentRepository,
                             ScheduledTimeRepository scheduledTimeRepository,
                             ResourceAccessService resourceAccessService,
                             TreatmentProgressService progressService) {
        this.medicationRepository = medicationRepository;
        this.treatmentRepository = treatmentRepository;
        this.scheduledTimeRepository = scheduledTimeRepository;
        this.resourceAccessService = resourceAccessService;
        this.progressService = progressService;
    }

    public List<MedicationResponse> getAllMedications() {
        if (!resourceAccessService.isAuthenticated()) {
            return medicationRepository.findAll().stream().map(this::toResponse).toList();
        }
        User user = resourceAccessService.currentUser();
        List<Medication> medications = user.getType() == UserType.DOCTOR
                ? medicationRepository.findByTreatment_Doctor_Id(resourceAccessService.currentDoctor().getId())
                : medicationRepository.findByTreatment_Patient_Id(resourceAccessService.currentPatient().getId());
        return medications.stream().map(this::toResponse).toList();
    }

    public MedicationResponse getMedicationById(Long medicationId) {
        Medication medication = findMedication(medicationId);
        resourceAccessService.requireMedicationReadAccess(medication);
        return toResponse(medication);
    }

    public List<MedicationResponse> getMedicationsByTreatmentId(Long treatmentId) {
        Treatment treatment = ensureTreatmentExists(treatmentId);
        resourceAccessService.requireTreatmentReadAccess(treatment);
        return medicationRepository.findByTreatmentId(treatmentId).stream().map(this::toResponse).toList();
    }

    public List<MedicationResponse> getMedicationsByPatientId(Long patientId) {
        if (!resourceAccessService.isAuthenticated()) {
            return medicationRepository.findByTreatment_Patient_Id(patientId)
                    .stream().map(this::toResponse).toList();
        }
        User user = resourceAccessService.currentUser();
        List<Medication> medications;
        if (user.getType() == UserType.DOCTOR) {
            Long doctorId = resourceAccessService.currentDoctor().getId();
            medications = medicationRepository
                    .findByTreatment_Patient_IdAndTreatment_Doctor_Id(patientId, doctorId);
        } else {
            Long authenticatedPatientId = resourceAccessService.currentPatient().getId();
            requireSameId(patientId, authenticatedPatientId, "Patient access denied");
            medications = medicationRepository.findByTreatment_Patient_Id(patientId);
        }
        return medications.stream().map(this::toResponse).toList();
    }

    @Transactional
    public MedicationResponse createMedication(Long treatmentId, MedicationRequest request) {
        Treatment treatment = ensureTreatmentExists(treatmentId);
        resourceAccessService.requireTreatmentWriteAccess(treatment);

        Medication medication = new Medication();
        medication.setTreatment(treatment);
        medication.setPatient(treatment.getPatient());
        medication.setDoctor(treatment.getDoctor());
        applyRequest(medication, request, true);

        Medication saved = medicationRepository.save(medication);
        replaceScheduledTimes(saved, request.getScheduledTimes());
        progressService.recalculate(treatmentId);

        return toResponse(saved);
    }

    @Transactional
    public MedicationResponse updateMedication(Long medicationId, MedicationRequest request) {
        Medication medication = findMedication(medicationId);
        resourceAccessService.requireMedicationWriteAccess(medication);
        applyRequest(medication, request, false);

        Medication saved = medicationRepository.save(medication);
        if (request.getScheduledTimes() != null) {
            replaceScheduledTimes(saved, request.getScheduledTimes());
        }
        progressService.recalculate(saved.getTreatment().getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteMedication(Long medicationId) {
        Medication medication = findMedication(medicationId);
        resourceAccessService.requireMedicationWriteAccess(medication);
        Long treatmentId = medication.getTreatment().getId();
        medicationRepository.delete(medication);
        medicationRepository.flush();
        progressService.recalculate(treatmentId);
    }

    private Medication findMedication(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
    }

    private Treatment ensureTreatmentExists(Long treatmentId) {
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment not found"));
    }

    private void applyRequest(Medication medication, MedicationRequest request, boolean create) {
        if (create || request.getName() != null) {
            medication.setName(required(request.getName(), "Medication name is required"));
        }
        if (create || request.getDosage() != null) {
            medication.setDosage(request.getDosage());
        }
        if (create || request.getFrequency() != null) {
            medication.setFrequency(request.getFrequency());
        }
        if (create) {
            medication.setPrescribedBy(medication.getDoctor().getUser().getName());
        }
        if (create || request.getStartDate() != null) {
            medication.setStartDate(request.getStartDate());
        }
        if (create || request.getEndDate() != null) {
            medication.setEndDate(request.getEndDate());
        }
        if (create || request.getInstructions() != null) {
            medication.setInstructions(request.getInstructions());
        }
        if (create || request.getUnitType() != null) {
            medication.setUnitType(Optional.ofNullable(request.getUnitType())
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(String::toUpperCase)
                    .orElse("PILL"));
        }
        if (create) {
            medication.setPillsRemaining(0);
            medication.setLowStockThreshold(5);
        }
        if (medication.getSideEffects() == null) {
            medication.setSideEffects("");
        }
        validateMedication(medication);
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private void replaceScheduledTimes(Medication medication, List<String> times) {
        scheduledTimeRepository.deleteByMedicationId(medication.getId());
        if (times == null || times.isEmpty()) {
            medication.setScheduledTimes(new ArrayList<>());
            return;
        }

        Set<String> normalizedTimes = new LinkedHashSet<>();
        for (String time : times) {
            if (time == null || time.trim().isEmpty()) {
                continue;
            }
            String normalizedTime = time.trim();
            if (!TIME_PATTERN.matcher(normalizedTime).matches()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Scheduled times must use HH:mm format");
            }
            normalizedTimes.add(normalizedTime);
        }

        List<ScheduledTime> scheduledTimes = new ArrayList<>();
        for (String time : normalizedTimes) {
            ScheduledTime scheduledTime = new ScheduledTime();
            scheduledTime.setMedication(medication);
            scheduledTime.setTime(time);
            scheduledTimes.add(scheduledTime);
        }
        medication.setScheduledTimes(scheduledTimeRepository.saveAll(scheduledTimes));
    }

    private void validateMedication(Medication medication) {
        if (medication.getStartDate() != null && medication.getEndDate() != null
                && medication.getEndDate().isBefore(medication.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Medication end date cannot be before start date");
        }
        requireNonNegative(medication.getPillsRemaining(), "Remaining pills cannot be negative");
        requireNonNegative(medication.getLowStockThreshold(), "Low stock threshold cannot be negative");
    }

    private void requireNonNegative(Integer value, String message) {
        if (value != null && value < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void requireSameId(Long requestedId, Long authenticatedId, String message) {
        if (!java.util.Objects.equals(requestedId, authenticatedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private MedicationResponse toResponse(Medication medication) {
        Long treatmentId = medication.getTreatment() == null ? null : medication.getTreatment().getId();
        List<String> scheduledTimes = scheduledTimeRepository.findByMedicationId(medication.getId())
                .stream()
                .map(ScheduledTime::getTime)
                .toList();

        return new MedicationResponse(
                medication.getId(),
                treatmentId,
                medication.getPatient().getId(),
                medication.getDoctor().getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getPrescribedBy(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.getInstructions(),
                medication.getUnitType(),
                medication.getPillsRemaining(),
                medication.getLowStockThreshold(),
                medication.getSideEffects(),
                scheduledTimes
        );
    }
}
