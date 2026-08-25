package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.*;

import java.util.List;

public interface CajaService {
    List<CajaResponse> listar();
    List<CajaResponse> listarPorSucursal(Integer idSucursal);
    CajaResponse obtenerPorId(Integer id);
    CajaResponse crear(CajaRequest request);
    CajaResponse actualizar(Integer id, CajaRequest request);
    void eliminar(Integer id);
    CajaResponse abrirCaja(Integer id, AperturaCajaRequest request);
    CajaResponse cerrarCaja(Integer id);
    CajaResponse ingresarEfectivo(Integer id, MovimientoCajaRequest request);
    CajaResponse retirarEfectivo(Integer id, MovimientoCajaRequest request);
    List<MovimientoCajaResponse> movimientos(Integer id);
    CorteResponse previewCorte(Integer id);
    CorteResponse realizarCorte(Integer id);
    CorteResponse actualizarDetallePagos(Integer idCorte, List<CorteDetallePagoUpdateRequest.ItemDetallePago> pagos);
}
