package com.bcsystems.bonds.dto;

import com.bcsystems.bonds.domain.en.TipoMovimientoCredito;

import java.time.LocalDateTime;

public record MovimientoCreditoResponse(
        Integer idMovimiento,
        Integer idCredito,
        TipoMovimientoCredito tipo,
        Double monto,
        Double saldoAnterior,
        Double saldoNuevo,
        String descripcion,
        LocalDateTime fecha,
        String usuario
) {}
