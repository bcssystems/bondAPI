package com.bcsystems.bonds.dto;

public record SucursalResponse(
    Integer idSucursal,
    String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
