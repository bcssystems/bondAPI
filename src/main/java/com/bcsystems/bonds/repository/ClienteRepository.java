package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    @Query("SELECT c FROM Cliente c WHERE " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.telefono) LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY c.nombre")
    Page<Cliente> buscar(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.nombre")
    Page<Cliente> findActivos(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.tieneCredito = true AND c.activo = true ORDER BY c.nombre")
    Page<Cliente> findCreditClients(Pageable pageable);
}
