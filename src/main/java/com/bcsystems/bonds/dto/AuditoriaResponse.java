package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(
    Integer idAuditoria,
    String entidad,
    Integer entidadId,
    String accion,
    String usuario,
    LocalDateTime fecha,
    String detalle,
    String referencia,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo
) {}
