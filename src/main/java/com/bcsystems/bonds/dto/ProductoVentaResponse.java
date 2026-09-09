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
    List<MultimediaResponse> multimedia,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record MultimediaResponse(Integer idMultimedia, String tipo, String url, String nombreArchivo, Boolean esPrincipal) {}
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
}