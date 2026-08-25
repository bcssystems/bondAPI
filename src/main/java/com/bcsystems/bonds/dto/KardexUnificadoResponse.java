package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record KardexUnificadoResponse(
    Long id,
    LocalDateTime fecha,
    String tipo,
    String entidad,
    String detalle,
    String usuario,
    String referencia,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String origen
) {}