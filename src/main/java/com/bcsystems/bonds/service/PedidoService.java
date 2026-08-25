package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.PedidoRequest;
import com.bcsystems.bonds.dto.PedidoResponse;
import com.bcsystems.bonds.dto.RecepcionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {
    Page<PedidoResponse> listar(String search, String estado, Integer idProveedor, Pageable pageable);
    PedidoResponse obtenerPorId(Integer id);
    PedidoResponse crear(PedidoRequest request);
    PedidoResponse cancelar(Integer id);
    PedidoResponse recibir(Integer id, RecepcionRequest request);
    PedidoResponse completar(Integer id);
}
