package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.AtributoRequest;
import com.bcsystems.bonds.dto.AtributoResponse;
import com.bcsystems.bonds.service.AtributoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    public ResponseEntity<List<AtributoResponse>> listar() {
        return ResponseEntity.ok(atributoService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<AtributoResponse>> listarActivos() {
        return ResponseEntity.ok(atributoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtributoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(atributoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AtributoResponse> crear(@Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atributoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtributoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.ok(atributoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        atributoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
