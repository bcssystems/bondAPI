package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.PedidoRequest;
import com.bcsystems.bonds.dto.PedidoResponse;
import com.bcsystems.bonds.dto.RecepcionRequest;
import com.bcsystems.bonds.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idProveedor,
            @PageableDefault(size = 10, sort = "idPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listar(search, estado, idProveedor, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @PostMapping("/{id}/recibir")
    public ResponseEntity<PedidoResponse> recibir(@PathVariable Integer id, @Valid @RequestBody RecepcionRequest request) {
        return ResponseEntity.ok(pedidoService.recibir(id, request));
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<PedidoResponse> completar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.completar(id));
    }
}
