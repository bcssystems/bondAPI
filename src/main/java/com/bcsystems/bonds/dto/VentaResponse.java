package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Integer idVenta,
        Integer idCaja,
        String cajaNombre,
        Integer idSucursal,
        String sucursalNombre,
        Integer idCliente,
        String clienteNombre,
        String usuario,
        String tipoVenta,
        Integer precioSeleccionado,
        Double subtotal,
        Double descuento,
        Double total,
        String estado,
        String nota,
        LocalDateTime fecha,
        List<VentaDetalleResponse> detalles,
        List<VentaPagoResponse> pagos
) {}
