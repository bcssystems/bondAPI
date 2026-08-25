package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PromocionDetalleRequest(
    @NotNull Integer idProducto,
    @Min(1) Integer cantidad
) {}
