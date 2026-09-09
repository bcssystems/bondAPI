package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.PermisoResponse;
import com.bcsystems.bonds.dto.RolRequest;
import com.bcsystems.bonds.dto.RolResponse;

import java.util.List;
import java.util.Map;

public interface RolService {
    List<RolResponse> listar();
    RolResponse obtenerPorId(Integer id);
    RolResponse crear(RolRequest request);
    RolResponse actualizar(Integer id, RolRequest request);
    void eliminar(Integer id);
    RolResponse reactivar(Integer id);
    Map<String, List<PermisoResponse>> permisosPorModulo();
}