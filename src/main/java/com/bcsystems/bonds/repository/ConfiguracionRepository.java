package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
    Optional<Configuracion> findByClave(String clave);
}