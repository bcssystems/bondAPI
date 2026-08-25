package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.AuditoriaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditoriaService {
    Page<AuditoriaResponse> listar(String entidad, String usuario, Pageable pageable);
    void registrar(String entidad, Integer entidadId, String accion, String usuario, String detalle);
    void registrarMovimiento(String entidad, Integer entidadId, String accion, String usuario,
                             String detalle, String referencia, Integer cantidad,
                             Integer stockAnterior, Integer stockNuevo);
}
