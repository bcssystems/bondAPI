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
    String unidadMedida,
    Double metrosPorRollo,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<MultimediaResponse> multimedia,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record MultimediaResponse(Integer idMultimedia, String tipo, String url, String nombreArchivo, Boolean esPrincipal) {}
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
}