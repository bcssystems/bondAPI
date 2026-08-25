package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.KardexUnificadoResponse;
import com.bcsystems.bonds.dto.MovimientoStockResponse;
import com.bcsystems.bonds.service.KardexService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/kardex")
public class KardexController {

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    @GetMapping
    public ResponseEntity<Page<MovimientoStockResponse>> listarMovimientos(
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(kardexService.listarMovimientos(idProducto, idSucursal, fechaInicio, fechaFin, pageable));
    }

    @GetMapping("/todo")
    public ResponseEntity<Page<KardexUnificadoResponse>> listarTodo(
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(kardexService.listarTodo(idSucursal, fechaInicio, fechaFin, tipo, pageable));
    }
}
