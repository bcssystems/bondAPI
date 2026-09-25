package com.bcsystems.bonds.dto;

import java.util.List;

public record ProductoVentaResponse(
    Integer idProducto,
    String sku,
    String nombre,
    Double precioBase,
    Integer stockActual,
    Double costoPromedio,
    Boolean activo,
    String unidadMedida,
    Double metrosPorRollo,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
}