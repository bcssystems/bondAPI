package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByClave(String clave);

    boolean existsByClave(String clave);

    List<Permiso> findAllByOrderByModuloAscClaveAsc();

    @Query("select distinct p.modulo from Permiso p order by p.modulo")
    List<String> findAllModulos();
}