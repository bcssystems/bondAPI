package com.bcsystems.bonds.auth;

import com.bcsystems.bonds.domain.en.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String usuario,
        @NotBlank String password,
        @NotNull Rol rol
) {}
