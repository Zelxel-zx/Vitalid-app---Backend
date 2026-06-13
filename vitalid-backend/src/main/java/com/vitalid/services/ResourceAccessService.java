package com.vitalid.services;

import com.vitalid.models.Doctor;
import com.vitalid.models.Medication;
import com.vitalid.models.Patient;
import com.vitalid.models.Treatment;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class ResourceAccessService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public ResourceAccessService(UserRepository userRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName());
    }

    public Doctor currentDoctor() {
        User user = currentUser();
        if (user.getType() != UserType.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can perform this operation");
        }

        Doctor doctor = doctorRepository.findByUser_Id(user.getId());
        if (doctor == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor profile not found");
        }
        return doctor;
    }

    public Patient currentPatient() {
        User user = currentUser();
        if (user.getType() != UserType.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can perform this operation");
        }

        return patientRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Patient profile not found"));
    }

    public void requireTreatmentReadAccess(Treatment treatment) {
        if (!isAuthenticated()) {
            return;
        }
        User user = currentUser();
        if (user.getType() == UserType.DOCTOR
                && Objects.equals(treatment.getDoctor().getUser().getId(), user.getId())) {
            return;
        }
        if (user.getType() == UserType.PATIENT
                && Objects.equals(treatment.getPatient().getUser().getId(), user.getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Treatment access denied");
    }

    public void requireTreatmentWriteAccess(Treatment treatment) {
        if (!isAuthenticated()) {
            return;
        }
        Doctor doctor = currentDoctor();
        if (!Objects.equals(treatment.getDoctor().getId(), doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Treatment access denied");
        }
    }

    public void requireMedicationReadAccess(Medication medication) {
        requireTreatmentReadAccess(medication.getTreatment());
    }

    public void requireMedicationWriteAccess(Medication medication) {
        requireTreatmentWriteAccess(medication.getTreatment());
    }
}
