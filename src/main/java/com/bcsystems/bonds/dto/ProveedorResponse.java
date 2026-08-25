package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record ProveedorResponse(
    Integer idProveedor,
    String nombre,
    String rfc,
    String telefono,
    String email,
    String direccion,
    String contactoNombre,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {}
