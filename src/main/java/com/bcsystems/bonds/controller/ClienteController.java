package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(clienteService.listar(null, null, 0, 1));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<Page<ClienteResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clienteService.listar(search, activo, page, size));
    }

    @GetMapping("/lista-negra")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<List<ClienteResponse>> listarListaNegra() {
        return ResponseEntity.ok(clienteService.listarEnListaNegra());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTES_CREAR')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_EDITAR')")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @PutMapping("/{id}/lista-negra")
    @PreAuthorize("hasAuthority('CLIENTES_EDITAR')")
    public ResponseEntity<ClienteResponse> cambiarListaNegra(
            @PathVariable Integer id, @Valid @RequestBody ListaNegraRequest request) {
        return ResponseEntity.ok(clienteService.cambiarListaNegra(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ine")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<ClienteIneResponse> obtenerIne(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerIne(id));
    }

    @PostMapping(value = "/{id}/ine", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CLIENTES_EDITAR')")
    public ResponseEntity<ClienteIneResponse> subirIne(
            @PathVariable Integer id,
            @RequestParam(value = "frontal", required = false) MultipartFile frontal,
            @RequestParam(value = "trasera", required = false) MultipartFile trasera) {
        return ResponseEntity.ok(clienteService.subirIne(id, frontal, trasera));
    }
}
