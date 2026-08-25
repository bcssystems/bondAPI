package com.bcsystems.bonds.dto;

import java.util.List;

public record AtributoResponse(
    Integer idAtributo,
    String nombre,
    Boolean activo,
    List<AtributoValorResponse> valores
) {
    public record AtributoValorResponse(Integer idValor, String valor, String codigoSku, Boolean activo) {}
}
