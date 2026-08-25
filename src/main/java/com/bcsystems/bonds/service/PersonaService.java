package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.PersonaRequest;
import com.bcsystems.bonds.dto.PersonaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonaService {
    Page<PersonaResponse> listar(Pageable pageable);
    PersonaResponse obtenerPorId(Integer id);
    PersonaResponse crear(PersonaRequest request);
    PersonaResponse actualizar(Integer id, PersonaRequest request);
    void eliminar(Integer id);
}
