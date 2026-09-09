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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('PROMOCIONES_VER')")
    public ResponseEntity<Page<PromocionResponse>> listar(
            @RequestParam(required = false) TipoPromocion tipo,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(promocionService.listar(tipo, activo, pageable));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('PROMOCIONES_VER')")
    public ResponseEntity<List<PromocionResponse>> listarActivas() {
        return ResponseEntity.ok(promocionService.listarActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOCIONES_VER')")
    public ResponseEntity<PromocionResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(promocionService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOCIONES_CREAR')")
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOCIONES_EDITAR')")
    public ResponseEntity<PromocionResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.ok(promocionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOCIONES_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
