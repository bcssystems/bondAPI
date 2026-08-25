package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record AbonoResponse(
        Integer idAbono,
        Integer idCredito,
        Double monto,
        String tipo,
        LocalDateTime fecha,
        String usuario
) {}
