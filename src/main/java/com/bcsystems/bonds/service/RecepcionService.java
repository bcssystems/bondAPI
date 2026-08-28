package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.RecepcionRequest;
import com.bcsystems.bonds.dto.RecepcionResponse;

import java.util.List;
import java.util.Map;

public interface RecepcionService {
    List<RecepcionResponse> listar(String search, Integer idSucursal);
    List<RecepcionResponse> listarTodos();
    RecepcionResponse obtenerPorId(Integer id);
    RecepcionResponse crear(RecepcionRequest request);
    void eliminar(Integer id);
    Map<String, Object> stats();
}