package com.vitalid.services;

import java.util.stream.Collectors;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vitalid.dtos.auth.LoginRequest;
import com.vitalid.dtos.auth.RegisterRequest;
import com.vitalid.dtos.auth.AuthResponse;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.exception.InvalidCredentialsException;
import com.vitalid.repositories.UserRepository;
import com.vitalid.exception.ResourceNotFoundException;
import com.vitalid.models.Doctor;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
            // Verificar si el email o telÃ©fono ya existen
            // Crear nuevo usuario
            // Guardar en la base de datos
            // Generar token JWT
            // Retornar AuthResponse con datos del usuario y token
        if(userRepository.existsByEmail(request.email())) {
            throw new InvalidCredentialsException("El email ya estÃ¡ registrado");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setType(request.type());

        User savedUser = userRepository.save(user);

        if (savedUser.getType() == UserType.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setStatus("OFFLINE");
            doctor.setVerified(false);
            doctor.setExperienceYears(0);
            doctor.setUnreadMessages(0);
            doctor.setSpecialty("General"); // Default specialty
            doctorRepository.save(doctor);
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

        String token = jwtTokenProvider.generateToken(user.getEmail());
        
        return new AuthResponse(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getType(), 
            user.getCreatedAt(),
            token, 
            "Inicio de sesión exitoso",
            user.isProfileCompleted());
    }

    // 3. Validar token
    public boolean validateToken(String token) {
        // Verificar si el token es vÃ¡lido
        return jwtTokenProvider.validateToken(token);
    }
    
    // 4. Refrescar token
    public String refreshToken(String token) {
        // Generar nuevo token
        System.out.println("DEBUG: Starting refreshToken with token: " + token.substring(0, 20) + "...");
        
        if(validateToken(token)) {
            System.out.println("DEBUG: Token is valid");
            String email = jwtTokenProvider.getEmailFromToken(token);
            System.out.println("DEBUG: Email extracted from token: " + email);
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
            System.out.println("DEBUG: User found: " + user.getEmail());
            
            String newToken = jwtTokenProvider.generateToken(user.getEmail());
            System.out.println("DEBUG: New token generated successfully");
            return newToken;
        }
        System.out.println("DEBUG: Token validation failed");
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
        return new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getType(),
            user.getCreatedAt(),
            jwtTokenProvider.generateToken(user.getEmail()),
            "Operación exitosa",
            user.isProfileCompleted()
        );
    }

    public List<AuthResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}




