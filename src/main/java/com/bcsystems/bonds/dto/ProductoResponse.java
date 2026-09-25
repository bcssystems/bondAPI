package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoResponse(
    Integer idProducto,
    String sku,
    String nombre,
    String descripcion,
    Double precioBase,
    Integer stockActual,
    Integer stockMinimo,
    Integer stockMaximo,
    Double costoPromedio,
    Integer idCategoria,
    String categoriaNombre,
    String unidadMedida,
    Double metrosPorRollo,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
}