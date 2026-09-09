package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.KardexUnificadoResponse;
import com.bcsystems.bonds.dto.MovimientoStockResponse;
import com.bcsystems.bonds.domain.en.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface KardexService {
    Page<MovimientoStockResponse> listarMovimientos(Integer idProducto, Integer idSucursal, TipoMovimiento tipo,
                                                     LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                     Pageable pageable);
    Page<KardexUnificadoResponse> listarTodo(Integer idSucursal, Integer idProducto,
                                              LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                              String tipo, Pageable pageable);
}
