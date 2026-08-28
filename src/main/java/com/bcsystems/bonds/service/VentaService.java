package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.VentaRequest;
import com.bcsystems.bonds.dto.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    VentaResponse crear(VentaRequest request);
    VentaResponse obtenerPorId(Integer id);
    List<VentaResponse> listarPorCaja(Integer idCaja);
    Page<VentaResponse> listar(Integer idSucursal, Integer idCaja, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    VentaResponse cancelar(Integer id);
    VentaResponse solicitarCancelacion(Integer id, String motivo);
    VentaResponse rechazarCancelacion(Integer id);
    VentaResponse ponerEnEspera(Integer id);
    VentaResponse reanudar(Integer id);
    List<VentaResponse> ventasEnEspera(Integer idCaja);
    List<VentaResponse> listarPorSucursal(Integer idSucursal);
    VentaResponse ventaRapida(Integer idCaja, String descripcion, Double precioCompra, Double precioVenta, Integer cantidad, Integer idCliente);
}
