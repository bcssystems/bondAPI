package com.bcsystems.bonds.dto;

public record VentaDetalleRequest(
        Integer idProducto,
        String descripcion,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal,
        String atributosText
) {}
