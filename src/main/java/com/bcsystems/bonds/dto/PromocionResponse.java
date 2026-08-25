package com.bcsystems.bonds.dto;

import com.bcsystems.bonds.domain.en.TipoPromocion;

import java.time.LocalDateTime;
import java.util.List;

public record PromocionResponse(
    Integer idPromocion,
    String nombre,
    String descripcion,
    TipoPromocion tipo,
    Double descuentoPorcentaje,
    Double precioSugerido,
    Double precioFinal,
    Integer idProducto,
    String productoSku,
    String productoNombre,
    Boolean activo,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    LocalDateTime fechaCreacion,
    List<PromocionDetalleResponse> detalles
) {
    public record PromocionDetalleResponse(
        Integer idPromocionDetalle,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        Integer cantidad
    ) {}
}
