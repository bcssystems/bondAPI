package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.domain.en.TipoPromocion;
import com.bcsystems.bonds.dto.PromocionRequest;
import com.bcsystems.bonds.dto.PromocionResponse;
import com.bcsystems.bonds.service.PromocionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public ResponseEntity<Page<PromocionResponse>> listar(
            @RequestParam(required = false) TipoPromocion tipo,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(promocionService.listar(tipo, activo, pageable));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PromocionResponse>> listarActivas() {
        return ResponseEntity.ok(promocionService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(promocionService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.ok(promocionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
