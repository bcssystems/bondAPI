package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfiguracionRequest(
        @NotBlank String clave,
        @NotBlank String valor,
        String descripcion
) {}