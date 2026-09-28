package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.PrecioCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrecioClienteRepository extends JpaRepository<PrecioCliente, Integer> {
    List<PrecioCliente> findByClienteIdCliente(Integer idCliente);

    Optional<PrecioCliente> findByClienteIdClienteAndProductoIdProducto(Integer idCliente, Integer idProducto);

    List<PrecioCliente> findByProductoIdProducto(Integer idProducto);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PrecioCliente pc where pc.cliente.idCliente = :idCliente")
    void deleteByClienteIdCliente(@Param("idCliente") Integer idCliente);

    long countByClienteIdCliente(Integer idCliente);
}