package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ConfiguracionRequest;
import com.bcsystems.bonds.dto.ConfiguracionResponse;

import java.util.List;

public interface ConfiguracionService {
    List<ConfiguracionResponse> listar();
    ConfiguracionResponse obtener(String clave);
    String getValor(String clave, String valorDefault);
    int getValorInt(String clave, int valorDefault);
    double getValorDouble(String clave, double valorDefault);
    ConfiguracionResponse actualizar(String clave, ConfiguracionRequest request);
}