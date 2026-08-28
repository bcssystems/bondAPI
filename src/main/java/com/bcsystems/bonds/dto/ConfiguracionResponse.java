package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record ConfiguracionResponse(
        Integer idConfiguracion,
        String clave,
        String valor,
        String descripcion,
        LocalDateTime actualizadaEn
) {}