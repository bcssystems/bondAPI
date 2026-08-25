package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.service.CajaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajas")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping
    public ResponseEntity<List<CajaResponse>> listar() {
        return ResponseEntity.ok(cajaService.listar());
    }

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<List<CajaResponse>> listarPorSucursal(@PathVariable Integer idSucursal) {
        return ResponseEntity.ok(cajaService.listarPorSucursal(idSucursal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CajaResponse> crear(@Valid @RequestBody CajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CajaResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody CajaRequest request) {
        return ResponseEntity.ok(cajaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        cajaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/apertura")
    public ResponseEntity<CajaResponse> abrirCaja(@PathVariable Integer id, @Valid @RequestBody AperturaCajaRequest request) {
        return ResponseEntity.ok(cajaService.abrirCaja(id, request));
    }

    @PostMapping("/{id}/cierre")
    public ResponseEntity<CajaResponse> cerrarCaja(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.cerrarCaja(id));
    }

    @PostMapping("/{id}/ingresos")
    public ResponseEntity<CajaResponse> ingresarEfectivo(@PathVariable Integer id, @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.ok(cajaService.ingresarEfectivo(id, request));
    }

    @PostMapping("/{id}/egresos")
    public ResponseEntity<CajaResponse> retirarEfectivo(@PathVariable Integer id, @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.ok(cajaService.retirarEfectivo(id, request));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoCajaResponse>> movimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.movimientos(id));
    }

    @GetMapping("/{id}/corte-preview")
    public ResponseEntity<CorteResponse> previewCorte(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.previewCorte(id));
    }

    @PostMapping("/{id}/corte")
    public ResponseEntity<CorteResponse> realizarCorte(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.realizarCorte(id));
    }
}
