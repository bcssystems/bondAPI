package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Permiso;
import com.bcsystems.bonds.domain.PermisoAdicional;
import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.domain.Rol;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.dto.PersonaRequest;
import com.bcsystems.bonds.dto.PersonaResponse;
import com.bcsystems.bonds.dto.RolResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.PermisoAdicionalRepository;
import com.bcsystems.bonds.repository.PermisoRepository;
import com.bcsystems.bonds.repository.PersonaRepository;
import com.bcsystems.bonds.repository.RolRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.PersonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PermisoAdicionalRepository permisoAdicionalRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                               PasswordEncoder passwordEncoder,
                               AuditoriaService auditoriaService,
                               RolRepository rolRepository,
                               PermisoRepository permisoRepository,
                               PermisoAdicionalRepository permisoAdicionalRepository) {
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.permisoAdicionalRepository = permisoAdicionalRepository;
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

        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new InvalidEntryException("Rol no encontrado"));

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new InvalidEntryException("El rol seleccionado está inactivo");
        }

        Persona persona = Persona.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .usuario(request.usuario())
                .password(request.password() != null ? passwordEncoder.encode(request.password()) : null)
                .rol(rol)
                .activa(request.activa() != null ? request.activa() : true)
                .build();

        persona = personaRepository.save(persona);

        if (request.permisosAdicionales() != null && !request.permisosAdicionales().isEmpty()) {
            guardarPermisosAdicionales(persona, request.permisosAdicionales());
        }

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

        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new InvalidEntryException("Rol no encontrado"));

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new InvalidEntryException("El rol seleccionado está inactivo");
        }

        persona.setNombre(request.nombre());
        persona.setApellido(request.apellido());
        persona.setUsuario(request.usuario());
        persona.setRol(rol);
        if (request.password() != null && !request.password().isBlank()) {
            persona.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.activa() != null) persona.setActiva(request.activa());

        personaRepository.save(persona);

        permisoAdicionalRepository.deleteByPersonaIdPersona(id);
        if (request.permisosAdicionales() != null && !request.permisosAdicionales().isEmpty()) {
            guardarPermisosAdicionales(persona, request.permisosAdicionales());
        }

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

    private void guardarPermisosAdicionales(Persona persona, List<Integer> permisosIds) {
        List<Permiso> permisos = permisoRepository.findAllById(permisosIds);
        for (Permiso permiso : permisos) {
            PermisoAdicional pa = PermisoAdicional.builder()
                    .persona(persona)
                    .permiso(permiso)
                    .build();
            permisoAdicionalRepository.save(pa);
        }
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private PersonaResponse toResponse(Persona p) {
        List<String> rolPermisos = p.getRol().getPermisos().stream()
                .map(Permiso::getClave)
                .sorted()
                .collect(Collectors.toList());
        RolResponse rolResponse = new RolResponse(
                p.getRol().getIdRol(),
                p.getRol().getNombre(),
                p.getRol().getDescripcion(),
                p.getRol().getEsSistema(),
                p.getRol().getActivo(),
                rolPermisos
        );

        List<Integer> permisosAdicionales = new ArrayList<>();
        if (p.getPermisosAdicionales() != null) {
            permisosAdicionales = p.getPermisosAdicionales().stream()
                    .map(pa -> pa.getPermiso().getIdPermiso())
                    .collect(Collectors.toList());
        }

        return new PersonaResponse(p.getIdPersona(), p.getNombre(), p.getApellido(),
                p.getUsuario(), rolResponse, p.getActiva(), p.getFechaRegistro(), permisosAdicionales);
    }
}
