package com.bcsystems.bonds.dto;

public record CategoriaRequest(
        Integer idCategoria,
        String nombre,
        String descripcion,
        Boolean activo
) {}
