package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Sucursal;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.dto.SucursalRequest;
import com.bcsystems.bonds.dto.SucursalResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.SucursalRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.SucursalService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final AuditoriaService auditoriaService;

    public SucursalServiceImpl(SucursalRepository sucursalRepository, AuditoriaService auditoriaService) {
        this.sucursalRepository = sucursalRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public List<SucursalResponse> listarTodas() {
        return sucursalRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SucursalResponse obtenerPorId(Integer id) {
        return toResponse(sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id)));
    }

    @Transactional
    @Override
    public SucursalResponse crear(SucursalRequest request) {
        if (sucursalRepository.count() >= 1) {
            throw new InvalidEntryException("El sistema solo admite una sucursal registrada");
        }

        if (sucursalRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new InvalidEntryException("Ya existe una sucursal con ese nombre");
        }

        Sucursal sucursal = Sucursal.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .activa(request.activa() != null ? request.activa() : true)
                .build();

        sucursal = sucursalRepository.save(sucursal);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("SUCURSAL", sucursal.getIdSucursal(), AccionAuditoria.CREACION.name(), usuario,
                "Se creó la sucursal: " + sucursal.getNombre());

        return toResponse(sucursal);
    }

    @Transactional
    @Override
    public SucursalResponse actualizar(Integer id, SucursalRequest request) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id));

        if (sucursalRepository.existsByNombreIgnoreCaseAndIdSucursalNot(request.nombre(), id)) {
            throw new InvalidEntryException("Ya existe otra sucursal con ese nombre");
        }

        sucursal.setNombre(request.nombre());
        sucursal.setDireccion(request.direccion());
        sucursal.setTelefono(request.telefono());
        if (request.activa() != null) sucursal.setActiva(request.activa());

        sucursalRepository.save(sucursal);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("SUCURSAL", id, AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualizó la sucursal: " + sucursal.getNombre());

        return toResponse(sucursal);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id));
        sucursal.setActiva(false);
        sucursalRepository.save(sucursal);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("SUCURSAL", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se eliminó la sucursal: " + sucursal.getNombre());
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private SucursalResponse toResponse(Sucursal s) {
        return new SucursalResponse(s.getIdSucursal(), s.getNombre(), s.getDireccion(),
                s.getTelefono(), s.getActiva());
    }
}
