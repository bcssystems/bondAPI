package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

public record TipoPagoRequest(
        @NotBlank String nombre
) {}
