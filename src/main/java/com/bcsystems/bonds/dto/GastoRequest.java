package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GastoRequest(
        @NotNull Integer idCaja,
        @NotBlank String descripcion,
        @NotNull Double monto
) {}
