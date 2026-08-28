package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.RecepcionRequest;
import com.bcsystems.bonds.dto.RecepcionResponse;
import com.bcsystems.bonds.service.RecepcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recepciones")
public class RecepcionController {

    private final RecepcionService recepcionService;

    public RecepcionController(RecepcionService recepcionService) {
        this.recepcionService = recepcionService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(recepcionService.stats());
    }

    @GetMapping
    public ResponseEntity<List<RecepcionResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal) {
        return ResponseEntity.ok(recepcionService.listar(search, idSucursal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecepcionResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(recepcionService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<RecepcionResponse> crear(@Valid @RequestBody RecepcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recepcionService.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        recepcionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}