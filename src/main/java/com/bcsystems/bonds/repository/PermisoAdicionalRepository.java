package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.PermisoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisoAdicionalRepository extends JpaRepository<PermisoAdicional, Integer> {

    List<PermisoAdicional> findByPersonaIdPersona(Integer idPersona);

    @Modifying
    @Query("delete from PermisoAdicional pa where pa.persona.idPersona = :idPersona")
    void deleteByPersonaIdPersona(@Param("idPersona") Integer idPersona);
}