package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.SucursalRequest;
import com.bcsystems.bonds.dto.SucursalResponse;

import java.util.List;

public interface SucursalService {
    List<SucursalResponse> listarTodas();
    SucursalResponse obtenerPorId(Integer id);
    SucursalResponse crear(SucursalRequest request);
    SucursalResponse actualizar(Integer id, SucursalRequest request);
    void eliminar(Integer id);
}
