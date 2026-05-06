package com.vitalid.auth.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.vitalid.auth.dto.AuthResponse;
import com.vitalid.auth.dto.LoginRequest;
import com.vitalid.auth.dto.RegisterRequest;
import com.vitalid.auth.service.AuthService;
import com.vitalid.auth.exception.InvalidCredentialsException;
import com.vitalid.exception.ApiResponse;

/**
 * Authentication Controller
 * Handles user registration, login, and token refresh
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro y autenticacion de usuarios")
public class AuthController {
    // TODO: Implement authentication endpoints
    // POST /api/auth/register - Register new user
    // POST /api/auth/login - User login
    // POST /api/auth/refresh-token - Refresh JWT token
    // POST /api/auth/logout - User logout
    // GET /api/auth/verify - Verify token

    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
    @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
    @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshToken(
    @RequestHeader("Authorization") String authHeader) {
    
        // Validar que el header exista
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Token no proporcionado");
        }
        
        // Extraer token (elimina "Bearer ")
        String token = authHeader.substring(7);
        
        // Generar nuevo token
        String newToken = authService.refreshToken(token);
        
        return ResponseEntity.ok(ApiResponse.ok("Token refrescado exitosamente", newToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logout exitoso"));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyToken(
    @RequestHeader("Authorization") String authHeader) {
    
        // Validar que el header exista
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.ok(false));
        }
        
        // Extraer token
        String token = authHeader.substring(7);
        
        // Validar token
        boolean isValid = authService.validateToken(token);
        
        return ResponseEntity.ok(ApiResponse.ok(isValid));
    }

}

