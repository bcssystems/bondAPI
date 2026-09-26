package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AbonoCorteDto(
        Integer idAbono,
        Integer idCredito,
        String folioCredito,
        Integer idCliente,
        String cliente,
        LocalDateTime fecha,
        String tipoPago,
        Double monto,
        List<AbonoPagoResponse> pagos
) {}