package com.vitalid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.contraints.Password;

@Data
@AllArgsConstructor
@NoArgsConstructor
public record LoginRequest (

    @Email(message = "Email inválido")
    @NotBlank(message = "Email es obligatorio")
    String email,

    @Password(message = "Contraseña es obligatoria")
    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "Contraseña debe tener entre 6 y 100 caracteres")
    String password

) {}
