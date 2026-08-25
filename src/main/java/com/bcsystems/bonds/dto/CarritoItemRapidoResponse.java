package com.bcsystems.bonds.dto;

public record CarritoItemRapidoResponse(
        Integer idItemRapido,
        Integer idCaja,
        String descripcion,
        Double precioVenta,
        Double precioCompra,
        Integer cantidad
) {}
