package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

public record SucursalRequest(
    @NotBlank String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
