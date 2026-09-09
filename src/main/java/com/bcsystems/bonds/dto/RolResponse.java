package com.bcsystems.bonds.dto;

import java.util.List;

public record RolResponse(
    Integer idRol,
    String nombre,
    String descripcion,
    Boolean esSistema,
    Boolean activo,
    List<String> permisos
) {}