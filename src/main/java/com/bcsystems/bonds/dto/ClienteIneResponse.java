package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record ClienteIneResponse(
        Integer idClienteIne,
        Integer idCliente,
        String urlFotoFrontal,
        String urlFotoTrasera,
        String nombreArchivoFrontal,
        String nombreArchivoTrasera,
        LocalDateTime subidoEn
) {}