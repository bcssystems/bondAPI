package com.bcsystems.bonds.dto;

public record TipoPagoResponse(
        Integer idTipoPago,
        String nombre,
        Boolean activo
) {}
