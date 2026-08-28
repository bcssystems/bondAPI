package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Credito;
import com.bcsystems.bonds.domain.en.EstadoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository extends JpaRepository<Credito, Integer> {

    Optional<Credito> findByVentaIdVenta(Integer idVenta);

    List<Credito> findByClienteIdClienteAndEstadoOrderByFechaCreacionDesc(Integer idCliente, EstadoCredito estado);

    boolean existsByFolio(String folio);

    List<Credito> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);

    @Query("SELECT c FROM Credito c WHERE c.cliente.idCliente = :idCliente ORDER BY c.fechaCreacion DESC")
    List<Credito> findActivosByCliente(@Param("idCliente") Integer idCliente);
}
