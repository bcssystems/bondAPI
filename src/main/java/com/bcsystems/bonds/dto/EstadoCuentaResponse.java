package com.bcsystems.bonds.dto;

import java.util.List;

public record EstadoCuentaResponse(
        CreditoResponse credito,
        ClienteResponse cliente,
        List<AbonoResponse> abonos,
        List<MovimientoCreditoResponse> movimientos,
        String titularPagare,
        double tasaInteresMora
) {}