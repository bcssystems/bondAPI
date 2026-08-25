package com.bcsystems.bonds.service;

import com.bcsystems.bonds.domain.en.TipoPromocion;
import com.bcsystems.bonds.dto.PromocionRequest;
import com.bcsystems.bonds.dto.PromocionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromocionService {
    Page<PromocionResponse> listar(TipoPromocion tipo, Boolean activo, Pageable pageable);
    PromocionResponse obtenerPorId(Integer id);
    PromocionResponse crear(PromocionRequest request);
    PromocionResponse actualizar(Integer id, PromocionRequest request);
    void eliminar(Integer id);
    List<PromocionResponse> listarActivas();
}
