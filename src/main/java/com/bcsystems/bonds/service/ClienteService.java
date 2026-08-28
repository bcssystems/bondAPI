package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ClienteIneResponse;
import com.bcsystems.bonds.dto.ClienteRequest;
import com.bcsystems.bonds.dto.ClienteResponse;
import com.bcsystems.bonds.dto.ListaNegraRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClienteService {
    Page<ClienteResponse> listar(String search, int page, int size);
    Page<ClienteResponse> listarCreditClients(int page, int size);
    ClienteResponse obtenerPorId(Integer id);
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Integer id, ClienteRequest request);
    void eliminar(Integer id);
    List<ClienteResponse> listarEnListaNegra();
    ClienteResponse cambiarListaNegra(Integer id, ListaNegraRequest request);
    ClienteIneResponse subirIne(Integer id, MultipartFile frontal, MultipartFile trasera);
    ClienteIneResponse obtenerIne(Integer id);
}
