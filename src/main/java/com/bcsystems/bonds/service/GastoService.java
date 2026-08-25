package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.GastoRequest;
import com.bcsystems.bonds.dto.GastoResponse;

import java.util.List;

public interface GastoService {
    GastoResponse solicitar(GastoRequest request);
    List<GastoResponse> pendientes();
    List<GastoResponse> listarTodos();
    List<GastoResponse> listarPorCaja(Integer idCaja);
    long contarPendientesPorCaja(Integer idCaja);
    GastoResponse autorizar(Integer idGasto);
    GastoResponse rechazar(Integer idGasto);
}
