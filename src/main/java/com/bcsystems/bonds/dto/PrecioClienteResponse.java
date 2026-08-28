package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record PrecioClienteResponse(
        Integer idPrecioCliente,
        Integer idCliente,
        String clienteNombre,
        Integer idProducto,
        String productoNombre,
        String sku,
        Double precio,
        LocalDateTime actualizadoEn
) {}