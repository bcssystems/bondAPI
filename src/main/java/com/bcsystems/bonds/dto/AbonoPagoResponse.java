package com.bcsystems.bonds.dto;

public record AbonoPagoResponse(
        Integer idAbonoPago,
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        String referencia
) {}