package com.bcsystems.bonds.dto;

public record VentaPagoResponse(
        Integer idVentaPago,
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        String referencia
) {}
