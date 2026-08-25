package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Integer idPedido,
    String folio,
    Integer idProveedor,
    String proveedorNombre,
    String proveedorRfc,
    Integer idSucursal,
    String sucursalNombre,
    Integer idPersona,
    String personaNombre,
    String estado,
    String nota,
    List<PedidoDetalleResponse> detalles,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {
    public record PedidoDetalleResponse(
        Integer idPedidoDetalle,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        Integer cantidadPedida,
        Double precioCompraUnitario,
        Integer cantidadRecibida,
        Double costoUltimo,
        Double subtotal
    ) {}
}
