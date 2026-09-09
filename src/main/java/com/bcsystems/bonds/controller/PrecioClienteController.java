package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.PrecioClienteRequest;
import com.bcsystems.bonds.dto.PrecioClienteResponse;
import com.bcsystems.bonds.service.PrecioClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes/{idCliente}/precios")
public class PrecioClienteController {

    private final PrecioClienteService precioClienteService;

    public PrecioClienteController(PrecioClienteService precioClienteService) {
        this.precioClienteService = precioClienteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_VER')")
    public ResponseEntity<List<PrecioClienteResponse>> listar(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(precioClienteService.listarPorCliente(idCliente));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_EDITAR')")
    public ResponseEntity<List<PrecioClienteResponse>> guardar(
            @PathVariable Integer idCliente,
            @Valid @RequestBody List<PrecioClienteRequest> precios) {
        return ResponseEntity.ok(precioClienteService.guardarPreciosCliente(idCliente, precios));
    }

    @DeleteMapping("/{idPrecioCliente}")
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_EDITAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idPrecioCliente) {
        precioClienteService.eliminarPrecio(idPrecioCliente);
        return ResponseEntity.noContent().build();
    }
}