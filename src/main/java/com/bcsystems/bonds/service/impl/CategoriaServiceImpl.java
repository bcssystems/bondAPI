package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Categoria;
import com.bcsystems.bonds.dto.CategoriaRequest;
import com.bcsystems.bonds.dto.CategoriaResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.CategoriaRepository;
import com.bcsystems.bonds.service.CategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(String search, Boolean activo, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return categoriaRepository.buscar(search, pageable).map(this::toResponse);
        }
        if (activo != null) {
            return categoriaRepository.findByActivo(activo, pageable).map(this::toResponse);
        }
        return categoriaRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findAllActivas().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new InvalidEntryException("El nombre es obligatorio");
        }
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new InvalidEntryException("Ya existe una categor\u00eda con ese nombre");
        }
        Categoria cat = Categoria.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion() != null ? request.descripcion().trim() : null)
                .activo(request.activo() != null ? request.activo() : true)
                .build();
        cat = categoriaRepository.save(cat);
        return toResponse(cat);
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Integer id, CategoriaRequest request) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categor\u00eda no encontrada"));
        if (request.nombre() != null && !request.nombre().isBlank()) {
            if (!cat.getNombre().equalsIgnoreCase(request.nombre().trim()) &&
                    categoriaRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
                throw new InvalidEntryException("Ya existe otra categor\u00eda con ese nombre");
            }
            cat.setNombre(request.nombre().trim());
        }
        if (request.descripcion() != null) {
            cat.setDescripcion(request.descripcion().trim());
        }
        if (request.activo() != null) {
            cat.setActivo(request.activo());
        }
        cat = categoriaRepository.save(cat);
        return toResponse(cat);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categor\u00eda no encontrada"));
        cat.setActivo(false);
        categoriaRepository.save(cat);
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(
                c.getIdCategoria(), c.getNombre(), c.getDescripcion(),
                c.getActivo(), c.getFechaCreacion());
    }
}
