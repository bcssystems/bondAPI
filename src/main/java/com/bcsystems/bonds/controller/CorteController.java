package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.domain.CorteCaja;
import com.bcsystems.bonds.dto.CorteDetallePagoDto;
import com.bcsystems.bonds.dto.CorteDetallePagoUpdateRequest;
import com.bcsystems.bonds.dto.CorteResponse;
import com.bcsystems.bonds.repository.CorteCajaRepository;
import com.bcsystems.bonds.repository.CorteDetallePagoRepository;
import com.bcsystems.bonds.service.CajaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cortes")
public class CorteController {

    private final CorteCajaRepository corteCajaRepository;
    private final CorteDetallePagoRepository corteDetallePagoRepository;
    private final CajaService cajaService;

    public CorteController(CorteCajaRepository corteCajaRepository,
                           CorteDetallePagoRepository corteDetallePagoRepository,
                           CajaService cajaService) {
        this.corteCajaRepository = corteCajaRepository;
        this.corteDetallePagoRepository = corteDetallePagoRepository;
        this.cajaService = cajaService;
    }

    @GetMapping
    public ResponseEntity<Page<CorteResponse>> listar(
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @PageableDefault(size = 20, sort = "fechaCierre", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CorteCaja> page = corteCajaRepository.listarConFiltros(idSucursal, idCaja, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(page.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorteResponse> obtenerPorId(@PathVariable Integer id) {
        CorteCaja corte = corteCajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado con id: " + id));
        return ResponseEntity.ok(toResponse(corte));
    }

    @PutMapping("/{id}/detalle-pagos")
    public ResponseEntity<CorteResponse> actualizarDetallePagos(
            @PathVariable Integer id,
            @RequestBody CorteDetallePagoUpdateRequest request) {
        return ResponseEntity.ok(cajaService.actualizarDetallePagos(id, request.pagos()));
    }

    private CorteResponse toResponse(CorteCaja c) {
        List<CorteDetallePagoDto> detallePagos = corteDetallePagoRepository.findByCorteIdCorte(c.getIdCorte())
                .stream().map(d -> new CorteDetallePagoDto(
                        d.getTipoPago().getIdTipoPago(),
                        d.getTipoPago().getNombre(),
                        d.getMonto(),
                        d.getMontoReal()))
                .toList();
        double totalReal = detallePagos.stream()
                .filter(d -> d.montoReal() != null)
                .mapToDouble(CorteDetallePagoDto::montoReal)
                .sum();
        double sistema = detallePagos.stream().mapToDouble(CorteDetallePagoDto::monto).sum();
        double diferencia = totalReal > 0 ? totalReal - sistema : 0.0;
        return new CorteResponse(
                c.getIdCorte(), c.getCaja().getIdCaja(), c.getCaja().getNombre(),
                c.getCaja().getSucursal().getIdSucursal(), c.getCaja().getSucursal().getNombre(),
                c.getSaldoInicial(), c.getTotalVentas(),
                c.getTotalVentasContado(), c.getTotalVentasCredito(),
                c.getTotalIngresos(), c.getTotalEgresos(),
                c.getSaldoFinalContado(), null,
                c.getFechaApertura(), c.getFechaCierre(),
                c.getUsuario().getUsuario(), detallePagos,
                totalReal > 0 ? totalReal : null,
                totalReal > 0 ? diferencia : null);
    }
}
