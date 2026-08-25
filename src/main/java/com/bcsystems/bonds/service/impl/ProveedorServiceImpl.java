package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Proveedor;
import com.bcsystems.bonds.dto.ProveedorRequest;
import com.bcsystems.bonds.dto.ProveedorResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ProveedorRepository;
import com.bcsystems.bonds.service.ProveedorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public List<ProveedorResponse> listarActivos() {
        return proveedorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProveedorResponse> listar(String search, Boolean activo, Pageable pageable) {
        return proveedorRepository.buscarConFiltros(search, activo, pageable)
                .map(this::toResponse);
    }

    @Override
    public ProveedorResponse obtenerPorId(Integer id) {
        return toResponse(proveedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado con id: " + id)));
    }

    @Transactional
    @Override
    public ProveedorResponse crear(ProveedorRequest request) {
        if (proveedorRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new InvalidEntryException("Ya existe un proveedor con ese nombre");
        }

        Proveedor p = Proveedor.builder()
                .nombre(request.nombre())
                .rfc(request.rfc())
                .telefono(request.telefono())
                .email(request.email())
                .direccion(request.direccion())
                .contactoNombre(request.contactoNombre())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        p = proveedorRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    @Override
    public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado con id: " + id));

        if (proveedorRepository.existsByNombreIgnoreCaseAndIdProveedorNot(request.nombre(), id)) {
            throw new InvalidEntryException("Ya existe otro proveedor con ese nombre");
        }

        p.setNombre(request.nombre());
        p.setRfc(request.rfc());
        p.setTelefono(request.telefono());
        p.setEmail(request.email());
        p.setDireccion(request.direccion());
        p.setContactoNombre(request.contactoNombre());
        if (request.activo() != null) p.setActivo(request.activo());

        proveedorRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado con id: " + id));
        p.setActivo(false);
        proveedorRepository.save(p);
    }

    private ProveedorResponse toResponse(Proveedor p) {
        return new ProveedorResponse(
                p.getIdProveedor(), p.getNombre(), p.getRfc(),
                p.getTelefono(), p.getEmail(), p.getDireccion(),
                p.getContactoNombre(), p.getActivo(),
                p.getFechaCreacion(), p.getFechaActualizacion());
    }
}
