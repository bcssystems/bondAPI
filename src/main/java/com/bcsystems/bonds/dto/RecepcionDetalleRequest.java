package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record RecepcionDetalleRequest(
        @NotNull Integer idProducto,
        @NotNull Integer metros,
        @NotNull Double precioCompra
) {}