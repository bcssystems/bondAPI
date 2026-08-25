package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CotizacionResponse(
        Integer idCotizacion,
        Integer idCliente,
        String clienteNombre,
        Integer idUsuario,
        String usuarioNombre,
        String paqueteria,
        Boolean cobraEnvio,
        Double montoEnvio,
        Integer precioSeleccionado,
        Integer diasVigencia,
        Double total,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaExpiracion,
        String tipoVenta,
        Integer plazoMeses,
        Double porcentajeInteres,
        List<CotizacionDetalleResponse> detalles
) {}
