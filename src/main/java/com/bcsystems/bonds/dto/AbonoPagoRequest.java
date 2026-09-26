package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record AbonoPagoRequest(
        @NotNull Integer idTipoPago,
        @NotNull Double monto,
        String referencia
) {}