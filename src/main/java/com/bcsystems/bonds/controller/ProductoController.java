package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ProductoService.ProductoStats> stats() {
        return ResponseEntity.ok(productoService.obtenerStats());
    }

    @GetMapping("/stats/costo-por-sucursal")
    public ResponseEntity<List<Map<String, Object>>> costoPorSucursal() {
        return ResponseEntity.ok(productoService.costoPorSucursal());
    }

    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer idSucursal,
            @PageableDefault(size = 10, sort = "idProducto", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(search, activo, idSucursal, pageable));
    }

    @GetMapping("/para-venta")
    public ResponseEntity<Page<ProductoVentaResponse>> listarParaVenta(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal,
            @PageableDefault(size = 50, sort = "sku", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productoService.listarParaVenta(search, idSucursal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id) {
        productoService.reactivar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/multimedia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponse> agregarMultimedia(
            @PathVariable Integer id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "false") Boolean esPrincipal) {
        return ResponseEntity.ok(productoService.agregarMultimedia(id, archivo, esPrincipal));
    }

    @DeleteMapping("/multimedia/{idMultimedia}")
    public ResponseEntity<Void> eliminarMultimedia(@PathVariable Integer idMultimedia) {
        productoService.eliminarMultimedia(idMultimedia);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/multimedia/{idMultimedia}/principal")
    public ResponseEntity<ProductoResponse> marcarPrincipal(
            @PathVariable Integer id, @PathVariable Integer idMultimedia) {
        return ResponseEntity.ok(productoService.marcarMultimediaPrincipal(id, idMultimedia));
    }

    @PutMapping("/{idProducto}/inventario-sucursal/{idSucursal}")
    public ResponseEntity<ProductoResponse> actualizarStockSucursal(
            @PathVariable Integer idProducto, @PathVariable Integer idSucursal,
            @RequestParam Integer stock) {
        return ResponseEntity.ok(productoService.actualizarStockSucursal(idProducto, idSucursal, stock));
    }

    @PostMapping("/{idProducto}/movimiento-stock")
    public ResponseEntity<ProductoResponse> registrarMovimiento(
            @PathVariable Integer idProducto, @Valid @RequestBody MovimientoStockRequest request) {
        return ResponseEntity.ok(productoService.registrarMovimientoStock(idProducto, request));
    }

    @PostMapping("/{idProducto}/transferir")
    public ResponseEntity<ProductoResponse> transferir(
            @PathVariable Integer idProducto, @Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.ok(productoService.transferirStock(idProducto, request));
    }
}
