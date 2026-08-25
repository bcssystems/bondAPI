package com.bcsystems.bonds.dto;

public record CorteDetallePagoDto(
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        Double montoReal
) {}
