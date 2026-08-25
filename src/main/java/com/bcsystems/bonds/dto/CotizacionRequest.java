package com.bcsystems.bonds.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CotizacionRequest(
        @NotNull Integer idCliente,
        String paqueteria,
        Boolean cobraEnvio,
        Double montoEnvio,
        Integer precioSeleccionado,
        @NotNull @Min(1) Integer diasVigencia,
        String tipoVenta,
        Integer plazoMeses,
        Double porcentajeInteres,
        @NotEmpty @Valid List<Detalle> detalles
) {
    public record Detalle(
            @NotNull Integer idProducto,
            @NotNull @Min(1) Integer cantidad,
            @NotNull Double precioUnitario
    ) {}
}
