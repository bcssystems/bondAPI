package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record AperturaCajaRequest(
        @NotNull Double saldoInicial
) {}
