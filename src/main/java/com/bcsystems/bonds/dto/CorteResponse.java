package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CorteResponse(
        Integer idCorte,
        Integer idCaja,
        String cajaNombre,
        Integer idSucursal,
        String sucursalNombre,
        Double saldoInicial,
        Double totalVentas,
        Double totalVentasContado,
        Double totalVentasCredito,
        Double totalIngresos,
        Double totalEgresos,
        Double totalGastos,
        Double totalAbonos,
        Double saldoFinalContado,
        Double saldoEsperado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        String usuario,
        List<CorteDetallePagoDto> detallePagos,
        List<GastoResponse> gastos,
        Double totalReal,
        Double diferencia,
        List<AbonoCorteDto> abonos
) {}
