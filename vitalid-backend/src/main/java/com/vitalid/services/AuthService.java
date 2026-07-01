package com.vitalid.services;

import com.vitalid.dtos.auth.AuthResponse;
import com.vitalid.dtos.auth.LoginRequest;
import com.vitalid.dtos.auth.RegisterRequest;
import com.vitalid.exception.InvalidCredentialsException;
import com.vitalid.exception.ResourceNotFoundException;
import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.UserRepository;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidCredentialsException("El email ya está registrado");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setType(request.type());

        User savedUser = userRepository.save(user);

        Long profileId = null;

        if (savedUser.getType() == UserType.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setStatus("OFFLINE");
            doctor.setVerified(false);
            doctor.setExperienceYears(0);
            doctor.setUnreadMessages(0);
            doctor.setSpecialty("General");
            doctor.setAvailabilityStart(LocalTime.of(9, 0));
            doctor.setAvailabilityEnd(LocalTime.of(17, 0));

            Doctor savedDoctor = doctorRepository.save(doctor);
            profileId = savedDoctor.getId();
        }

        if (savedUser.getType() == UserType.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setDateOfBirth(LocalDate.of(2000, 1, 1));
            patient.setBloodType("No especificado");
            patient.setIsActive(true);

            Patient savedPatient = patientRepository.save(patient);
            profileId = savedPatient.getId();
        }

        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return new AuthResponse(
            savedUser.getId(), 
            savedUser.getName(), 
            savedUser.getEmail(), 
            savedUser.getType(), 
            savedUser.getCreatedAt(),
            token, 
            "Usuario registrado exitosamente",
            savedUser.isProfileCompleted());  
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        boolean profileCompleted = resolveProfileCompleted(user);
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new AuthResponse(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getType(), 
            user.getCreatedAt(),
            token, 
            "Inicio de sesión exitoso",
            profileCompleted);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public String refreshToken(String token) {
        if (validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

            return jwtTokenProvider.generateToken(user.getEmail());
        }

        throw new InvalidCredentialsException("Invalid token");
    }

    public void logout() {
        // Se configura en el frontend eliminando el token del almacenamiento local
    }

    public void recoverPassword(String email, String newPassword) {
        if (email == null || email.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidCredentialsException("Email and new password are required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AuthResponse updateUser(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    private AuthResponse toResponse(User user) {
        boolean profileCompleted = resolveProfileCompleted(user);
        return new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getType(),
            user.getCreatedAt(),
            jwtTokenProvider.generateToken(user.getEmail()),
            "Operación exitosa",
            profileCompleted
        );
    }

    private boolean resolveProfileCompleted(User user) {
        if (user.isProfileCompleted()) {
            return true;
        }

        boolean hasCompletedProfile = switch (user.getType()) {
            case PATIENT -> patientRepository.findByUser_Id(user.getId()).isPresent();
            case DOCTOR -> {
                Doctor doctor = doctorRepository.findByUser_Id(user.getId());
                yield doctor != null
                    && doctor.getMedicalCenterAddress() != null
                    && !doctor.getMedicalCenterAddress().trim().isEmpty()
                    && doctor.getAvailabilityStart() != null
                    && doctor.getAvailabilityEnd() != null;
            }
        };

        if (hasCompletedProfile) {
            user.setProfileCompleted(true);
            userRepository.save(user);
        }

        return hasCompletedProfile;
    }

    public List<AuthResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}