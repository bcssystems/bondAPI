package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.dto.PersonaRequest;
import com.bcsystems.bonds.dto.PersonaResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.PersonaRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.PersonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                               PasswordEncoder passwordEncoder,
                               AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public Page<PersonaResponse> listar(Pageable pageable) {
        return personaRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public PersonaResponse obtenerPorId(Integer id) {
        return toResponse(personaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Persona no encontrada con id: " + id)));
    }

    @Transactional
    @Override
    public PersonaResponse crear(PersonaRequest request) {
        if (personaRepository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw new InvalidEntryException("El usuario ya existe");
        }

        Persona persona = Persona.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .usuario(request.usuario())
                .password(request.password() != null ? passwordEncoder.encode(request.password()) : null)
                .rol(request.rol())
                .activa(request.activa() != null ? request.activa() : true)
                .build();

        persona = personaRepository.save(persona);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PERSONA", persona.getIdPersona(), AccionAuditoria.CREACION.name(), usuario,
                "Se creó el usuario: " + persona.getUsuario());

        return toResponse(persona);
    }

    @Transactional
    @Override
    public PersonaResponse actualizar(Integer id, PersonaRequest request) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Persona no encontrada con id: " + id));

        if (personaRepository.existsByUsuarioIgnoreCaseAndIdPersonaNot(request.usuario(), id)) {
            throw new InvalidEntryException("El usuario ya está en uso");
        }

        persona.setNombre(request.nombre());
        persona.setApellido(request.apellido());
        persona.setUsuario(request.usuario());
        persona.setRol(request.rol());
        if (request.password() != null && !request.password().isBlank()) {
            persona.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.activa() != null) persona.setActiva(request.activa());

        personaRepository.save(persona);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PERSONA", id, AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualizó el usuario: " + persona.getUsuario());

        return toResponse(persona);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Persona no encontrada con id: " + id));
        persona.setActiva(false);
        personaRepository.save(persona);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PERSONA", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se eliminó el usuario: " + persona.getUsuario());
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private PersonaResponse toResponse(Persona p) {
        return new PersonaResponse(p.getIdPersona(), p.getNombre(), p.getApellido(),
                p.getUsuario(), p.getRol(), p.getActiva(), p.getFechaRegistro());
    }
}
