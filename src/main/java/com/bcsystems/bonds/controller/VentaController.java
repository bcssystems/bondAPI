package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.SolicitudCancelacionRequest;
import com.bcsystems.bonds.dto.VentaEsperaRequest;
import com.bcsystems.bonds.dto.VentaRequest;
import com.bcsystems.bonds.dto.VentaResponse;
import com.bcsystems.bonds.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENTAS_VER')")
    public ResponseEntity<Page<VentaResponse>> listar(
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ventaService.listar(idSucursal, idCaja, estado, fechaInicio, fechaFin, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crear(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENTAS_VER')")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/caja/{idCaja}")
    @PreAuthorize("hasAuthority('VENTAS_VER')")
    public ResponseEntity<List<VentaResponse>> listarPorCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(ventaService.listarPorCaja(idCaja));
    }

    @GetMapping("/sucursal/{idSucursal}")
    @PreAuthorize("hasAuthority('VENTAS_VER')")
    public ResponseEntity<List<VentaResponse>> listarPorSucursal(@PathVariable Integer idSucursal) {
        return ResponseEntity.ok(ventaService.listarPorSucursal(idSucursal));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('VENTAS_CANCELAR','CANCELACIONES_AUTORIZAR')")
    public ResponseEntity<VentaResponse> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.cancelar(id));
    }

    @PostMapping("/{id}/solicitar-cancelacion")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> solicitarCancelacion(
            @PathVariable Integer id, @Valid @RequestBody SolicitudCancelacionRequest request) {
        return ResponseEntity.ok(ventaService.solicitarCancelacion(id, request.motivo()));
    }

    @PostMapping("/{id}/rechazar-cancelacion")
    @PreAuthorize("hasAnyAuthority('VENTAS_CANCELAR','CANCELACIONES_AUTORIZAR')")
    public ResponseEntity<VentaResponse> rechazarCancelacion(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.rechazarCancelacion(id));
    }

    @PostMapping("/{id}/espera")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> ponerEnEspera(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.ponerEnEspera(id));
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> reanudar(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.reanudar(id));
    }

    @PostMapping("/{id}/cancelar-espera")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> cancelarEspera(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.cancelarEspera(id));
    }

    @PutMapping("/{id}/espera")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> actualizarEspera(@PathVariable Integer id,
                                                          @Valid @RequestBody VentaEsperaRequest request) {
        return ResponseEntity.ok(ventaService.actualizarEspera(id, request));
    }

    @GetMapping("/caja/{idCaja}/espera")
    @PreAuthorize("hasAuthority('VENTAS_VER')")
    public ResponseEntity<List<VentaResponse>> ventasEnEspera(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(ventaService.ventasEnEspera(idCaja));
    }

    @PostMapping("/caja/{idCaja}/rapida")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> ventaRapida(
            @PathVariable Integer idCaja,
            @RequestParam String descripcion,
            @RequestParam Double precioCompra,
            @RequestParam Double precioVenta,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) Integer idCliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ventaService.ventaRapida(idCaja, descripcion, precioCompra, precioVenta, cantidad, idCliente));
    }
}
