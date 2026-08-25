package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record AbonoGeneralRequest(
        @NotNull Integer idCliente,
        @NotNull Double monto
) {}
