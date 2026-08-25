package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.TipoPagoRequest;
import com.bcsystems.bonds.dto.TipoPagoResponse;
import com.bcsystems.bonds.service.TipoPagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-pago")
public class TipoPagoController {

    private final TipoPagoService tipoPagoService;

    public TipoPagoController(TipoPagoService tipoPagoService) {
        this.tipoPagoService = tipoPagoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoPagoResponse>> listarTodos() {
        return ResponseEntity.ok(tipoPagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoPagoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TipoPagoResponse> crear(@Valid @RequestBody TipoPagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoPagoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody TipoPagoRequest request) {
        return ResponseEntity.ok(tipoPagoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
