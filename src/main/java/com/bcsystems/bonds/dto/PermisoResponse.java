package com.bcsystems.bonds.dto;

public record PermisoResponse(
    Integer idPermiso,
    String clave,
    String nombre,
    String descripcion,
    String modulo
) {}