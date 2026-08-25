package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.AtributoValor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoValorRepository extends JpaRepository<AtributoValor, Integer> {
    List<AtributoValor> findByAtributoIdAtributoAndActivoTrue(Integer idAtributo);
    List<AtributoValor> findByIdValorIn(List<Integer> ids);
}
