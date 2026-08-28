package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecepcionResponse(
        Integer idRecepcion,
        String folio,
        Integer idProveedor,
        String proveedorNombre,
        String proveedorRfc,
        Integer idSucursal,
        String sucursalNombre,
        Integer idUsuario,
        String usuario,
        Double totalMetros,
        Integer totalRollos,
        String nota,
        LocalDateTime fechaRecepcion,
        List<RecepcionDetalleResponse> detalles
) {}