package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductoRequest(
    String sku,
    @NotBlank String nombre,
    String descripcion,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    Double costoPromedio,
    Boolean activo,
    Boolean tieneVariantes,
    Integer idProductoPadre,
    List<VarianteRequest> variantes,
    List<InventarioSucursalRequest> inventarios
) {
    public record VarianteRequest(
        Integer idVariante,
        String sku,
        String nombre,
        List<Integer> idAtributoValores,
        Double precio1,
        Double precio2,
        Double precio3,
        Double precio4,
        Boolean precioPersonalizado,
        Double costoPromedio,
        List<InventarioSucursalRequest> inventarios
    ) {}
}
