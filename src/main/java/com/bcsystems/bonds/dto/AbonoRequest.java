package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record AbonoRequest(
        @NotNull Integer idCredito,
        @NotNull Double monto,
        String tipo
) {}
