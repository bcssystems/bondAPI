package com.bcsystems.bonds.dto;

public record RecepcionDetalleResponse(
        Integer idRecepcionDetalle,
        Integer idProducto,
        String productoNombre,
        String sku,
        Integer metros,
        Integer rollos,
        Double precioCompra,
        Double subtotal
) {}