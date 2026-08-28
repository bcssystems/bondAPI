package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Abono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AbonoRepository extends JpaRepository<Abono, Integer> {

    List<Abono> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);

    Optional<Abono> findFirstByCreditoClienteIdClienteOrderByFechaDesc(Integer idCliente);

    @Query("SELECT MAX(a.fecha) FROM Abono a WHERE a.credito.cliente.idCliente = :idCliente")
    Optional<LocalDateTime> findMaxFechaByCliente(@Param("idCliente") Integer idCliente);
}
