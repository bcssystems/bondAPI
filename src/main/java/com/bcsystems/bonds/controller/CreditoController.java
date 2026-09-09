package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.service.ClienteService;
import com.bcsystems.bonds.service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;
    private final ClienteService clienteService;

    @GetMapping("/clientes")
    @PreAuthorize("hasAuthority('CREDITOS_VER')")
    public ResponseEntity<Page<ClienteResponse>> listarClientesCredito(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(clienteService.listar(search, page, size));
        }
        return ResponseEntity.ok(clienteService.listarCreditClients(page, size));
    }

    @GetMapping("/clientes/{id}/creditos")
    @PreAuthorize("hasAuthority('CREDITOS_VER')")
    public ResponseEntity<List<CreditoResponse>> listarCreditos(@PathVariable Integer id) {
        return ResponseEntity.ok(creditoService.listarCreditosPorCliente(id));
    }

    @GetMapping("/clientes/{id}/movimientos")
    @PreAuthorize("hasAuthority('CREDITOS_VER')")
    public ResponseEntity<List<MovimientoCreditoResponse>> listarMovimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(creditoService.listarMovimientosPorCliente(id));
    }

    @GetMapping("/{id}/estado-cuenta")
    @PreAuthorize("hasAuthority('CREDITOS_VER')")
    public ResponseEntity<EstadoCuentaResponse> estadoCuenta(@PathVariable Integer id) {
        return ResponseEntity.ok(creditoService.estadoCuenta(id));
    }

    @PostMapping("/abonos")
    @PreAuthorize("hasAuthority('CREDITOS_ABONAR')")
    public ResponseEntity<AbonoResponse> registrarAbono(@Valid @RequestBody AbonoRequest request) {
        return ResponseEntity.ok(creditoService.registrarAbono(request));
    }

    @PostMapping("/abonos/general")
    @PreAuthorize("hasAuthority('CREDITOS_ABONAR')")
    public ResponseEntity<List<AbonoResponse>> abonarATodas(@Valid @RequestBody AbonoGeneralRequest request) {
        return ResponseEntity.ok(creditoService.abonarATodas(request));
    }
}