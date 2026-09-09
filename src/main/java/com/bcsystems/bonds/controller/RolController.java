package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.PermisoResponse;
import com.bcsystems.bonds.dto.RolRequest;
import com.bcsystems.bonds.dto.RolResponse;
import com.bcsystems.bonds.service.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLES_VER','PERSONAS_CREAR','PERSONAS_EDITAR')")
    public ResponseEntity<List<RolResponse>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAnyAuthority('ROLES_VER','PERSONAS_CREAR','PERSONAS_EDITAR')")
    public ResponseEntity<Map<String, List<PermisoResponse>>> permisosPorModulo() {
        return ResponseEntity.ok(rolService.permisosPorModulo());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_VER')")
    public ResponseEntity<RolResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES_CREAR')")
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_EDITAR')")
    public ResponseEntity<RolResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody RolRequest request) {
        return ResponseEntity.ok(rolService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasAuthority('ROLES_ELIMINAR')")
    public ResponseEntity<RolResponse> reactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(rolService.reactivar(id));
    }
}