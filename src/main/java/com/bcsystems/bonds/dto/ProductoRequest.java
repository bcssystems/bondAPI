package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductoRequest(
    String sku,
    @NotBlank String nombre,
    String descripcion,
    Double precioBase,
    Double costoPromedio,
    @NotNull Integer idCategoria,
    String unidadMedida,
    Double metrosPorRollo,
    Boolean activo,
    List<InventarioSucursalRequest> inventarios
) {}