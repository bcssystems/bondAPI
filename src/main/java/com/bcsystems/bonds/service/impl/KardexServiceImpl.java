package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Auditoria;
import com.bcsystems.bonds.domain.MovimientoStock;
import com.bcsystems.bonds.dto.KardexUnificadoResponse;
import com.bcsystems.bonds.dto.MovimientoStockResponse;
import com.bcsystems.bonds.repository.AuditoriaRepository;
import com.bcsystems.bonds.repository.MovimientoStockRepository;
import com.bcsystems.bonds.service.KardexService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class KardexServiceImpl implements KardexService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaRepository auditoriaRepository;

    public KardexServiceImpl(MovimientoStockRepository movimientoStockRepository,
                              AuditoriaRepository auditoriaRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Page<MovimientoStockResponse> listarMovimientos(Integer idProducto, Integer idSucursal,
                                                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                            Pageable pageable) {
        return movimientoStockRepository.buscarConFiltros(idProducto, idSucursal, fechaInicio, fechaFin, pageable)
                .map(m -> new MovimientoStockResponse(
                        m.getIdMovimiento(),
                        m.getProducto().getIdProducto(),
                        m.getProducto().getSku(),
                        m.getProducto().getNombre(),
                        m.getSucursal() != null ? m.getSucursal().getIdSucursal() : null,
                        m.getSucursal() != null ? m.getSucursal().getNombre() : "Global",
                        m.getTipoMovimiento().name(),
                        m.getCantidad(),
                        m.getStockAnterior(),
                        m.getStockNuevo(),
                        m.getReferencia(),
                        m.getUsuario(),
                        m.getObservacion(),
                        m.getFechaMovimiento()
                ));
    }

    @Override
    public Page<KardexUnificadoResponse> listarTodo(Integer idSucursal, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                     String tipo, Pageable pageable) {
        List<MovimientoStock> movimientos = movimientoStockRepository
                .buscarMovimientosPorFechas(idSucursal, fechaInicio, fechaFin);
        List<Auditoria> auditorias = auditoriaRepository.buscarPorFechas(fechaInicio, fechaFin);

        AtomicLong counter = new AtomicLong(0);
        List<KardexUnificadoResponse> combined = new ArrayList<>();

        for (MovimientoStock m : movimientos) {
            combined.add(new KardexUnificadoResponse(
                    counter.incrementAndGet(),
                    m.getFechaMovimiento(),
                    m.getTipoMovimiento().name(),
                    "PRODUCTO (" + m.getProducto().getSku() + ")",
                    m.getObservacion() != null ? m.getObservacion() : m.getTipoMovimiento() + " de " + m.getCantidad() + " unidades",
                    m.getUsuario(),
                    m.getReferencia(),
                    m.getCantidad(),
                    m.getStockAnterior(),
                    m.getStockNuevo(),
                    "MOVIMIENTO"
            ));
        }

        for (Auditoria a : auditorias) {
            combined.add(new KardexUnificadoResponse(
                    counter.incrementAndGet(),
                    a.getFecha(),
                    a.getAccion().name(),
                    a.getEntidad() + (a.getEntidadId() != null ? " #" + a.getEntidadId() : ""),
                    a.getDetalle(),
                    a.getUsuario(),
                    a.getReferencia(),
                    a.getCantidad(),
                    a.getStockAnterior(),
                    a.getStockNuevo(),
                    "AUDITORIA"
            ));
        }

        if (tipo != null && !tipo.isBlank()) {
            combined = combined.stream()
                    .filter(item -> item.tipo().equalsIgnoreCase(tipo))
                    .collect(Collectors.toList());
        }

        combined.sort(Comparator.comparing(KardexUnificadoResponse::fecha).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        List<KardexUnificadoResponse> pageContent = start < combined.size()
                ? combined.subList(start, end)
                : List.of();

        return new PageImpl<>(pageContent, pageable, combined.size());
    }
}
