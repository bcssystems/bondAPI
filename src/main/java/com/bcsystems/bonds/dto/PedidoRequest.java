package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
    @NotNull Integer idProveedor,
    @NotNull Integer idSucursal,
    String nota,
    @NotNull List<PedidoDetalleRequest> detalles
) {
    public record PedidoDetalleRequest(
        @NotNull Integer idProducto,
        @NotNull Integer cantidadPedida,
        @NotNull Double precioCompraUnitario
    ) {}
}
