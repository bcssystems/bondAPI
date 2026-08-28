package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank String nombre,
        @NotBlank String apellidoPaterno,
        String apellidoMaterno,
        @NotBlank String telefono,
        String codigoPais,
        String whatsapp,
        String empresa,
        @NotBlank String regimenFiscal,
        String cp,
        String direccion,
        String calle,
        String numExt,
        String numInt,
        String colonia,
        String municipio,
        String estado,
        String rfc,
        String representanteLegal,
        String direccionEntrega,
        Boolean tieneCredito,
        Double limiteCredito
) {}