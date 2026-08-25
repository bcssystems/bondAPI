package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record MovimientoStockResponse(
    Integer idMovimiento,
    Integer idProducto,
    String productoSku,
    String productoNombre,
    Integer idSucursal,
    String sucursalNombre,
    String tipoMovimiento,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String referencia,
    String usuario,
    String observacion,
    LocalDateTime fechaMovimiento
) {}
