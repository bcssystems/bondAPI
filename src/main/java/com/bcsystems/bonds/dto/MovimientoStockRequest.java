package com.bcsystems.bonds.dto;

import com.bcsystems.bonds.domain.en.TipoMovimiento;
import jakarta.validation.constraints.NotNull;

public record MovimientoStockRequest(
    @NotNull TipoMovimiento tipoMovimiento,
    @NotNull Integer cantidad,
    Integer idSucursal,
    String referencia,
    String observacion
) {}
