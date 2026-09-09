package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RolRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull List<Integer> permisos
) {}