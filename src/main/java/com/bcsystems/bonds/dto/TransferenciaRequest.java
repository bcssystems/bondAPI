package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record TransferenciaRequest(
    @NotNull Integer idSucursalOrigen,
    @NotNull Integer idSucursalDestino,
    @NotNull Integer cantidad,
    String referencia,
    String observacion
) {}
