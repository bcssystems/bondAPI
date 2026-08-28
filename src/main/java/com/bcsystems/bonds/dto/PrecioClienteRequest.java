package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record PrecioClienteRequest(
        @NotNull Integer idProducto,
        @NotNull Double precio
) {}