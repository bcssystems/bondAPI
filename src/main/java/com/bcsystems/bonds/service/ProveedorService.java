package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ProveedorRequest;
import com.bcsystems.bonds.dto.ProveedorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProveedorService {
    List<ProveedorResponse> listarActivos();
    Page<ProveedorResponse> listar(String search, Boolean activo, Pageable pageable);
    ProveedorResponse obtenerPorId(Integer id);
    ProveedorResponse crear(ProveedorRequest request);
    ProveedorResponse actualizar(Integer id, ProveedorRequest request);
    void eliminar(Integer id);
}
