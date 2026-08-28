package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.PrecioCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrecioClienteRepository extends JpaRepository<PrecioCliente, Integer> {
    List<PrecioCliente> findByClienteIdCliente(Integer idCliente);

    Optional<PrecioCliente> findByClienteIdClienteAndProductoIdProducto(Integer idCliente, Integer idProducto);

    List<PrecioCliente> findByProductoIdProducto(Integer idProducto);

    void deleteByClienteIdCliente(Integer idCliente);

    long countByClienteIdCliente(Integer idCliente);
}