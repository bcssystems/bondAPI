package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecepcionRequest(
        @NotNull Integer idSucursal,
        Integer idProveedor,
        String nota,
        @NotEmpty List<RecepcionDetalleRequest> detalles
) {}