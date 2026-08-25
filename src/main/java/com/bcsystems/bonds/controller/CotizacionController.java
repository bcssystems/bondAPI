package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.CotizacionRequest;
import com.bcsystems.bonds.dto.CotizacionResponse;
import com.bcsystems.bonds.service.CotizacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CotizacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotizacionResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CotizacionResponse>> listar(@RequestParam(required = false) String estado) {
        if (estado != null && !estado.isEmpty()) {
            return ResponseEntity.ok(cotizacionService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CotizacionResponse> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.cancelar(id));
    }

    @PostMapping("/{id}/convertir")
    public ResponseEntity<CotizacionResponse> convertirAVenta(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.convertirAVenta(id));
    }
}
