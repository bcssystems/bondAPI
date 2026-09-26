package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AbonoResponse(
        Integer idAbono,
        Integer idCredito,
        Double monto,
        String tipo,
        LocalDateTime fecha,
        String usuario,
        String metodoPago,
        List<AbonoPagoResponse> pagos
) {}