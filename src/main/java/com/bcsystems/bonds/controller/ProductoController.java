package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('PRODUCTOS_VER')")
    public ResponseEntity<ProductoService.ProductoStats> stats() {
        return ResponseEntity.ok(productoService.obtenerStats());
    }

    @GetMapping("/stats/costo-por-sucursal")
    @PreAuthorize("hasAuthority('PRODUCTOS_VER')")
    public ResponseEntity<List<Map<String, Object>>> costoPorSucursal() {
        return ResponseEntity.ok(productoService.costoPorSucursal());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTOS_VER')")
    public ResponseEntity<Page<ProductoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCategoria,
            @PageableDefault(size = 10, sort = "idProducto", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(search, activo, idSucursal, idCategoria, pageable));
    }

    @GetMapping("/para-venta")
    @PreAuthorize("hasAuthority('PRODUCTOS_VER')")
    public ResponseEntity<Page<ProductoVentaResponse>> listarParaVenta(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCategoria,
            @PageableDefault(size = 50, sort = "sku", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productoService.listarParaVenta(search, idSucursal, idCategoria, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTOS_VER')")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTOS_CREAR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTOS_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyAuthority('PRODUCTOS_EDITAR','PRODUCTOS_CREAR')")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id) {
        productoService.reactivar(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idProducto}/inventario-sucursal/{idSucursal}")
    @PreAuthorize("hasAuthority('PRODUCTOS_MOVIMIENTO')")
    public ResponseEntity<ProductoResponse> actualizarStockSucursal(
            @PathVariable Integer idProducto, @PathVariable Integer idSucursal,
            @RequestParam Integer stock) {
        return ResponseEntity.ok(productoService.actualizarStockSucursal(idProducto, idSucursal, stock));
    }

    @PostMapping("/{idProducto}/movimiento-stock")
    @PreAuthorize("hasAuthority('PRODUCTOS_MOVIMIENTO')")
    public ResponseEntity<ProductoResponse> registrarMovimiento(
            @PathVariable Integer idProducto, @Valid @RequestBody MovimientoStockRequest request) {
        return ResponseEntity.ok(productoService.registrarMovimientoStock(idProducto, request));
    }

    @PostMapping("/{idProducto}/transferir")
    @PreAuthorize("hasAuthority('PRODUCTOS_MOVIMIENTO')")
    public ResponseEntity<ProductoResponse> transferir(
            @PathVariable Integer idProducto, @Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.ok(productoService.transferirStock(idProducto, request));
    }
}
