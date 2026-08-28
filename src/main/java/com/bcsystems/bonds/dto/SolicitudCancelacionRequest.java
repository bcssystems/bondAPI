package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCancelacionRequest(
        @NotBlank String motivo
) {}