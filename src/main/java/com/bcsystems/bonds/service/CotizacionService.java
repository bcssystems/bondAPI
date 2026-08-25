package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.CotizacionRequest;
import com.bcsystems.bonds.dto.CotizacionResponse;

import java.util.List;

public interface CotizacionService {
    CotizacionResponse crear(CotizacionRequest request);
    CotizacionResponse obtenerPorId(Integer id);
    List<CotizacionResponse> listarTodas();
    List<CotizacionResponse> listarPorEstado(String estado);
    CotizacionResponse convertirAVenta(Integer id);
    CotizacionResponse cancelar(Integer id);
}
