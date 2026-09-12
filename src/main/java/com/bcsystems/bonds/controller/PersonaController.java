package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.PersonaRequest;
import com.bcsystems.bonds.dto.PersonaResponse;
import com.bcsystems.bonds.service.PersonaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERSONAS_VER')")
    public ResponseEntity<Page<PersonaResponse>> listar(
            @RequestParam(required = false) Boolean activa,
            @PageableDefault(size = 10, sort = "idPersona", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(personaService.listar(activa, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAS_VER')")
    public ResponseEntity<PersonaResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERSONAS_CREAR')")
    public ResponseEntity<PersonaResponse> crear(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAS_EDITAR')")
    public ResponseEntity<PersonaResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAS_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
