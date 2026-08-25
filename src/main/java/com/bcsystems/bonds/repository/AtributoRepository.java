package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Atributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    List<Atributo> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdAtributoNot(String nombre, Integer idAtributo);
}
