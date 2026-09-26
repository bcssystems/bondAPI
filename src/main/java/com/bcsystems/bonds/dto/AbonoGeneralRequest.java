package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AbonoGeneralRequest(
        @NotNull Integer idCliente,
        @NotNull Double monto,
        Integer idTipoPago,
        Integer idCaja,
        List<AbonoPagoRequest> pagos
) {}