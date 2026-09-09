package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Permiso;
import com.bcsystems.bonds.domain.Rol;
import com.bcsystems.bonds.dto.PermisoResponse;
import com.bcsystems.bonds.dto.RolRequest;
import com.bcsystems.bonds.dto.RolResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.PermisoRepository;
import com.bcsystems.bonds.repository.RolRepository;
import com.bcsystems.bonds.service.RolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolServiceImpl(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RolResponse obtenerPorId(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id));
        return toResponse(rol);
    }

    @Transactional
    @Override
    public RolResponse crear(RolRequest request) {
        if (rolRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new InvalidEntryException("Ya existe un rol con ese nombre");
        }

        Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.permisos()));
        if (permisos.isEmpty()) {
            throw new InvalidEntryException("Debe seleccionar al menos un permiso");
        }

        Rol rol = Rol.builder()
                .nombre(request.nombre().toUpperCase())
                .descripcion(request.descripcion())
                .esSistema(false)
                .activo(true)
                .permisos(new ArrayList<>(permisos))
                .build();

        rol = rolRepository.save(rol);
        return toResponse(rol);
    }

    @Transactional
    @Override
    public RolResponse actualizar(Integer id, RolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id));

        if (rol.getEsSistema()) {
            throw new InvalidEntryException("No se pueden editar roles del sistema");
        }

        if (rolRepository.existsByNombreIgnoreCaseAndIdRolNot(request.nombre(), id)) {
            throw new InvalidEntryException("Ya existe un rol con ese nombre");
        }

        Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.permisos()));
        if (permisos.isEmpty()) {
            throw new InvalidEntryException("Debe seleccionar al menos un permiso");
        }

        rol.setNombre(request.nombre().toUpperCase());
        rol.setDescripcion(request.descripcion());
        rol.setPermisos(new ArrayList<>(permisos));

        rol = rolRepository.save(rol);
        return toResponse(rol);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id));

        if (rol.getEsSistema()) {
            throw new InvalidEntryException("No se pueden eliminar roles del sistema");
        }

        rol.setActivo(false);
        rolRepository.save(rol);
    }

    @Transactional
    @Override
    public RolResponse reactivar(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id));

        rol.setActivo(true);
        rol = rolRepository.save(rol);
        return toResponse(rol);
    }

    @Override
    public Map<String, List<PermisoResponse>> permisosPorModulo() {
        List<Permiso> permisos = permisoRepository.findAllByOrderByModuloAscClaveAsc();
        return permisos.stream()
                .map(p -> new PermisoResponse(p.getIdPermiso(), p.getClave(), p.getNombre(), p.getDescripcion(), p.getModulo()))
                .collect(Collectors.groupingBy(PermisoResponse::modulo, LinkedHashMap::new, Collectors.toList()));
    }

    private RolResponse toResponse(Rol rol) {
        List<String> permisos = rol.getPermisos().stream()
                .map(Permiso::getClave)
                .sorted()
                .collect(Collectors.toList());
        return new RolResponse(rol.getIdRol(), rol.getNombre(), rol.getDescripcion(), rol.getEsSistema(), rol.getActivo(), permisos);
    }
}