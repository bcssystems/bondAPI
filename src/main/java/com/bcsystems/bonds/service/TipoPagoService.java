package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.TipoPagoRequest;
import com.bcsystems.bonds.dto.TipoPagoResponse;

import java.util.List;

public interface TipoPagoService {
    List<TipoPagoResponse> listarTodos();
    TipoPagoResponse obtenerPorId(Integer id);
    TipoPagoResponse crear(TipoPagoRequest request);
    TipoPagoResponse actualizar(Integer id, TipoPagoRequest request);
    void eliminar(Integer id);
}
