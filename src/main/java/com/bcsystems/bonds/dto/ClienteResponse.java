package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
        Integer idCliente,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String codigoPais,
        String whatsapp,
        String empresa,
        String regimenFiscal,
        String cp,
        String direccion,
        Boolean activo,
        LocalDateTime fechaRegistro,
        Boolean tieneCredito,
        Double limiteCredito,
        Double saldoActual
) {}
