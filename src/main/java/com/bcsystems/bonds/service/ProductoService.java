package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService {
    Page<ProductoResponse> listar(String search, Boolean activo, Integer idSucursal, Integer idCategoria, Pageable pageable);
    Page<ProductoVentaResponse> listarParaVenta(String search, Integer idSucursal, Integer idCategoria, Pageable pageable);
    ProductoResponse obtenerPorId(Integer id);
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse actualizar(Integer id, ProductoRequest request);
    void eliminar(Integer id);
    void reactivar(Integer id);
    ProductoResponse actualizarStockSucursal(Integer idProducto, Integer idSucursal, Integer nuevoStock);
    ProductoResponse registrarMovimientoStock(Integer idProducto, MovimientoStockRequest request);
    ProductoResponse transferirStock(Integer idProducto, TransferenciaRequest request);
    ProductoStats obtenerStats();
    java.util.List<java.util.Map<String, Object>> costoPorSucursal();

    record ProductoStats(long total, long activos, long stockGlobal, long stockMinimo, double costoTotalInventario) {}
}
