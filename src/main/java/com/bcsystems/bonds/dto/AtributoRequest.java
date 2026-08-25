package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AtributoRequest(
    @NotBlank String nombre,
    Boolean activo,
    List<AtributoValorRequest> valores
) {
    public record AtributoValorRequest(
        Integer idValor,
        @NotBlank String valor,
        String codigoSku,
        Boolean activo
    ) {}
}
