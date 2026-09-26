package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AbonoRequest(
        @NotNull Integer idCredito,
        @NotNull Double monto,
        String tipo,
        Integer idTipoPago,
        Integer idCaja,
        List<AbonoPagoRequest> pagos
) {}