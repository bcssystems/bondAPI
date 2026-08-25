package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ReservaProductoResponse;

import java.util.List;

public interface ReservaProductoService {

    void reservar(Integer idCaja, Integer idProducto, Integer cantidad);

    void quitarReserva(Integer idCaja, Integer idProducto);

    void actualizarCantidad(Integer idCaja, Integer idProducto, Integer cantidad);

    void limpiarReservas(Integer idCaja);

    List<ReservaProductoResponse> obtenerReservasPorSucursal(Integer idSucursal);

    void limpiarExpiradas();
}
