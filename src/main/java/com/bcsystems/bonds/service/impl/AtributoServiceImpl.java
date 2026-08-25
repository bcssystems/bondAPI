package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Atributo;
import com.bcsystems.bonds.domain.AtributoValor;
import com.bcsystems.bonds.dto.AtributoRequest;
import com.bcsystems.bonds.dto.AtributoResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.AtributoRepository;
import com.bcsystems.bonds.repository.AtributoValorRepository;
import com.bcsystems.bonds.service.AtributoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AtributoServiceImpl implements AtributoService {

    private final AtributoRepository atributoRepository;
    private final AtributoValorRepository atributoValorRepository;

    public AtributoServiceImpl(AtributoRepository atributoRepository,
                               AtributoValorRepository atributoValorRepository) {
        this.atributoRepository = atributoRepository;
        this.atributoValorRepository = atributoValorRepository;
    }

    @Override
    public List<AtributoResponse> listar() {
        return atributoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AtributoResponse> listarActivos() {
        return atributoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AtributoResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Transactional
    @Override
    public AtributoResponse crear(AtributoRequest request) {
        if (atributoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new InvalidEntryException("Ya existe un atributo con el nombre: " + request.nombre());
        }

        Atributo atributo = Atributo.builder()
                .nombre(request.nombre())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        if (request.valores() != null) {
            for (var valReq : request.valores()) {
                AtributoValor valor = AtributoValor.builder()
                        .atributo(atributo)
                        .valor(valReq.valor())
                        .codigoSku(valReq.codigoSku())
                        .activo(valReq.activo() != null ? valReq.activo() : true)
                        .build();
                atributo.getValores().add(valor);
            }
        }

        atributo = atributoRepository.save(atributo);
        return toResponse(atributo);
    }

    @Transactional
    @Override
    public AtributoResponse actualizar(Integer id, AtributoRequest request) {
        Atributo atributo = buscarOExcepcion(id);

        if (atributoRepository.existsByNombreIgnoreCaseAndIdAtributoNot(request.nombre(), id)) {
            throw new InvalidEntryException("Ya existe un atributo con el nombre: " + request.nombre());
        }

        atributo.setNombre(request.nombre());
        if (request.activo() != null) atributo.setActivo(request.activo());

        if (request.valores() != null) {
            Map<Integer, AtributoValor> existingMap = new HashMap<>();
            for (AtributoValor v : atributo.getValores()) {
                if (v.getIdValor() != null) existingMap.put(v.getIdValor(), v);
            }

            Set<Integer> remainingIds = new HashSet<>();
            List<AtributoValor> nuevos = new ArrayList<>();

            for (var valReq : request.valores()) {
                if (valReq.idValor() != null && existingMap.containsKey(valReq.idValor())) {
                    AtributoValor v = existingMap.get(valReq.idValor());
                    v.setValor(valReq.valor());
                    v.setCodigoSku(valReq.codigoSku());
                    v.setActivo(valReq.activo() != null ? valReq.activo() : true);
                    remainingIds.add(valReq.idValor());
                } else {
                    nuevos.add(AtributoValor.builder()
                            .atributo(atributo)
                            .valor(valReq.valor())
                            .codigoSku(valReq.codigoSku())
                            .activo(valReq.activo() != null ? valReq.activo() : true)
                            .build());
                }
            }

            atributo.getValores().removeIf(v -> v.getIdValor() != null && !remainingIds.contains(v.getIdValor()));
            atributo.getValores().addAll(nuevos);
        }

        atributo = atributoRepository.save(atributo);
        return toResponse(atributo);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Atributo atributo = buscarOExcepcion(id);
        atributo.setActivo(false);
        atributoRepository.save(atributo);
    }

    private Atributo buscarOExcepcion(Integer id) {
        return atributoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Atributo no encontrado con id: " + id));
    }

    private AtributoResponse toResponse(Atributo a) {
        List<AtributoResponse.AtributoValorResponse> valores = a.getValores().stream()
                .map(v -> new AtributoResponse.AtributoValorResponse(
                        v.getIdValor(), v.getValor(), v.getCodigoSku(), v.getActivo()))
                .collect(Collectors.toList());

        return new AtributoResponse(a.getIdAtributo(), a.getNombre(), a.getActivo(), valores);
    }
}
