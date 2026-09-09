package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PersonaRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    @NotBlank String usuario,
    String password,
    @NotNull Integer idRol,
    List<Integer> permisosAdicionales,
    Boolean activa
) {}