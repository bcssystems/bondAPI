package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.ConfiguracionRequest;
import com.bcsystems.bonds.dto.ConfiguracionResponse;
import com.bcsystems.bonds.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuraciones")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<List<ConfiguracionResponse>> listar() {
        return ResponseEntity.ok(configuracionService.listar());
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ConfiguracionResponse> obtener(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.obtener(clave));
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionResponse> actualizar(
            @PathVariable String clave, @Valid @RequestBody ConfiguracionRequest request) {
        return ResponseEntity.ok(configuracionService.actualizar(clave, request));
    }
}