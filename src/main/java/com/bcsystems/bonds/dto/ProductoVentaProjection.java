package com.bcsystems.bonds.dto;

public record ProductoVentaProjection(
    Integer idProducto,
    String sku,
    String nombre,
    Double precioBase,
    Integer stockActual,
    Double costoPromedio,
    Boolean activo,
    String unidadMedida,
    Double metrosPorRollo
) {
}