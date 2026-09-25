package com.bcsystems.bonds.dto;

public record InventarioSucursalProjection(
    Integer id,
    Integer idProducto,
    Integer idSucursal,
    String sucursalNombre,
    Integer stock,
    Integer stockMinimo,
    Integer stockMaximo
) {
}