package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record CajaResponse(
        Integer idCaja,
        String nombre,
        String tipo,
        Integer idSucursal,
        String sucursalNombre,
        String estado,
        Double saldoActual,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        Boolean activa
) {}