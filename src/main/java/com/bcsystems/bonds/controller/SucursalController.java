package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.SucursalRequest;
import com.bcsystems.bonds.dto.SucursalResponse;
import com.bcsystems.bonds.service.SucursalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUCURSALES_VER')")
    public ResponseEntity<List<SucursalResponse>> listarTodas() {
        return ResponseEntity.ok(sucursalService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSALES_VER')")
    public ResponseEntity<SucursalResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUCURSALES_CREAR')")
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSALES_EDITAR')")
    public ResponseEntity<SucursalResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody SucursalRequest request) {
        return ResponseEntity.ok(sucursalService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSALES_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
