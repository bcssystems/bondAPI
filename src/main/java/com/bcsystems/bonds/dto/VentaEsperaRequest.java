package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaEsperaRequest(
        Integer idCliente,
        @NotNull Double subtotal,
        @NotNull Double descuento,
        @NotNull Double total,
        String nota,
        @NotEmpty List<VentaDetalleRequest> detalles
) {}