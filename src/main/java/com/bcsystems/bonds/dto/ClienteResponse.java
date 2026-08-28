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
        String calle,
        String numExt,
        String numInt,
        String colonia,
        String municipio,
        String estado,
        String rfc,
        String representanteLegal,
        String direccionEntrega,
        Boolean activo,
        LocalDateTime fechaRegistro,
        Boolean tieneCredito,
        Double limiteCredito,
        Double saldoActual,
        Boolean enListaNegra,
        LocalDateTime fechaListaNegra,
        String motivoListaNegra
) {}