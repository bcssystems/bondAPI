package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        Integer idMovimientoCaja,
        Integer idCaja,
        String cajaNombre,
        String tipo,
        Double monto,
        String motivo,
        String usuario,
        LocalDateTime fecha
) {}
