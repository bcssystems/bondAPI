package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ClienteRequest;
import com.bcsystems.bonds.dto.ClienteResponse;
import org.springframework.data.domain.Page;

public interface ClienteService {
    Page<ClienteResponse> listar(String search, int page, int size);
    Page<ClienteResponse> listarCreditClients(int page, int size);
    ClienteResponse obtenerPorId(Integer id);
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Integer id, ClienteRequest request);
    void eliminar(Integer id);
}
