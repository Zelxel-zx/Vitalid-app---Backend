package com.vitalid.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vitalid.auth.dto.LoginRequest;
import com.vitalid.auth.dto.RegisterRequest;
import com.vitalid.auth.dto.AuthResponse;
import com.vitalid.auth.entity.User;
import com.vitalid.auth.entity.UserType;
import com.vitalid.auth.exception.InvalidCredentialsException;
import com.vitalid.auth.repository.UserRepository;
import com.vitalid.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 * Authentication Service
 * Handles user registration and login
 * 
 * TODO: Implement methods:
 * - register(RegisterRequest) -> AuthResponse
 * - login(LoginRequest) -> AuthResponse
 * - logout()
 * - refreshToken(String token) -> String
 * - validateToken(String token) -> boolean
 */
@Service
public class AuthService {

    // TODO: Implement authentication business logic
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
            // Verificar si el email o teléfono ya existen
            // Crear nuevo usuario
            // Guardar en la base de datos
            // Generar token JWT
            // Retornar AuthResponse con datos del usuario y token
        if(userRepository.existsByEmail(request.email())) {
            throw new InvalidCredentialsException("El email ya está registrado");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setType(request.type());

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getType(), 
            token, 
            "Usuario registrado exitosamente");  
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
            token, 
            "Inicio de sesión exitoso");
    }

    // 3. Validar token
    public boolean validateToken(String token) {
        // Verificar si el token es válido
        return jwtTokenProvider.validateToken(token);
    }
    
    // 4. Refrescar token
    public String refreshToken(String token) {
        // Generar nuevo token
        if(validateToken(token)) {
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
}

