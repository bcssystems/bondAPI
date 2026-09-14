package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record AbonoCorteDto(
        Integer idAbono,
        Integer idCredito,
        String folioCredito,
        Integer idCliente,
        String cliente,
        LocalDateTime fecha,
        String tipoPago,
        Double monto
) {}