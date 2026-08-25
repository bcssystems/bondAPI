package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.GastoRequest;
import com.bcsystems.bonds.dto.GastoResponse;
import com.bcsystems.bonds.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gastos")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    public ResponseEntity<GastoResponse> solicitar(@Valid @RequestBody GastoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoService.solicitar(request));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<GastoResponse>> pendientes() {
        return ResponseEntity.ok(gastoService.pendientes());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<GastoResponse>> listarTodos() {
        return ResponseEntity.ok(gastoService.listarTodos());
    }

    @GetMapping("/caja/{idCaja}")
    public ResponseEntity<List<GastoResponse>> listarPorCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(gastoService.listarPorCaja(idCaja));
    }

    @GetMapping("/pendientes/{idCaja}/count")
    public ResponseEntity<Long> contarPendientesPorCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(gastoService.contarPendientesPorCaja(idCaja));
    }

    @PostMapping("/{idGasto}/autorizar")
    public ResponseEntity<GastoResponse> autorizar(@PathVariable Integer idGasto) {
        return ResponseEntity.ok(gastoService.autorizar(idGasto));
    }

    @PostMapping("/{idGasto}/rechazar")
    public ResponseEntity<GastoResponse> rechazar(@PathVariable Integer idGasto) {
        return ResponseEntity.ok(gastoService.rechazar(idGasto));
    }
}
