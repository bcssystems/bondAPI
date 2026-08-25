package com.bcsystems.bonds.dto;

import java.util.List;

public record CorteDetallePagoUpdateRequest(
        List<ItemDetallePago> pagos
) {
    public record ItemDetallePago(
            Integer idTipoPago,
            Double montoReal
    ) {}
}
