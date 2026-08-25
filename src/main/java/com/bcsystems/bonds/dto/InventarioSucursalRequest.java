package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record InventarioSucursalRequest(
    @NotNull Integer idSucursal,
    @NotNull Integer stock,
    Integer stockMinimo,
    Integer stockMaximo
) {}
