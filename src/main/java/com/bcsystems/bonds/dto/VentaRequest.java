package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequest(
        @NotNull Integer idCaja,
        Integer idCliente,
        @NotNull String tipoVenta,
        @NotNull Integer precioSeleccionado,
        @NotNull Double subtotal,
        @NotNull Double descuento,
        @NotNull Double total,
        String nota,
        Integer plazoMeses,
        Double porcentajeInteres,
        @NotEmpty List<VentaDetalleRequest> detalles,
        List<VentaPagoRequest> pagos
) {}
