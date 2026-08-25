package com.bcsystems.bonds.dto;

public record VentaDetalleResponse(
        Integer idVentaDetalle,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        String descripcion,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal,
        String atributosText
) {}
