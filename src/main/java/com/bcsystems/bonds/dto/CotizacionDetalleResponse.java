package com.bcsystems.bonds.dto;

public record CotizacionDetalleResponse(
        Integer idDetalle,
        Integer idProducto,
        String productoNombre,
        String productoSku,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {}
