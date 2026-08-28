package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.PrecioClienteRequest;
import com.bcsystems.bonds.dto.PrecioClienteResponse;

import java.util.List;

public interface PrecioClienteService {
    List<PrecioClienteResponse> listarPorCliente(Integer idCliente);

    List<PrecioClienteResponse> guardarPreciosCliente(Integer idCliente, List<PrecioClienteRequest> precios);

    void eliminarPrecio(Integer idPrecioCliente);

    Double obtenerPrecioEspecial(Integer idCliente, Integer idProducto);
}